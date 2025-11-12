package com.houseleasing.houseleasingmanagementsystem.controller;

import com.houseleasing.houseleasingmanagementsystem.model.MaintenanceRequest;
import com.houseleasing.houseleasingmanagementsystem.model.Review;
import com.houseleasing.houseleasingmanagementsystem.model.enums.MaintenanceStatus;
import com.houseleasing.houseleasingmanagementsystem.model.enums.ContractStatus;
import com.houseleasing.houseleasingmanagementsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private RentPaymentRepository rentPaymentRepository;

    @Autowired
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Map<String, Object> result = new HashMap<>();

        long totalHouses = houseRepository.count();
        // count rented houses by active contracts using ContractStatus
        long rentedHouses = contractRepository.findAllByStatus(ContractStatus.ACTIVE, PageRequest.of(0, 1)).getTotalElements();

        // monthly income - sum of rent payments in this month
        LocalDate now = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(now);
        LocalDate monthStart = thisMonth.atDay(1);
        LocalDate monthEnd = thisMonth.atEndOfMonth();
        double monthlyIncome = rentPaymentRepository.findByDueDateBetween(monthStart, monthEnd)
                .stream().mapToDouble(rp -> rp.getAmount() == null ? 0.0 : rp.getAmount()).sum();

        // monthly income series for last 6 months
        List<Map<String, Object>> series = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = thisMonth.minusMonths(i);
            LocalDate s = ym.atDay(1);
            LocalDate e = ym.atEndOfMonth();
            double sum = rentPaymentRepository.findByDueDateBetween(s, e)
                    .stream().mapToDouble(rp -> rp.getAmount() == null ? 0.0 : rp.getAmount()).sum();
            Map<String, Object> p = new HashMap<>();
            p.put("label", ym.getMonthValue() + "月");
            p.put("value", sum);
            series.add(p);
        }

        // pending maintenances
        // use the repository method that fetches the house association to avoid lazy init errors later
        List<MaintenanceRequest> pending = maintenanceRequestRepository.findByStatusWithHouse(MaintenanceStatus.PENDING, org.springframework.data.domain.PageRequest.of(0, 10)).getContent();

        // recent reviews - use existing paged fetch
        List<Review> reviews = reviewRepository.findAllWithRelations(PageRequest.of(0,5)).getContent();

        result.put("totalHouses", totalHouses);
        result.put("rentedHouses", rentedHouses);
        result.put("activeTenants", userRepository.count());
        result.put("monthlyIncome", monthlyIncome);
        result.put("monthlyIncomeSeries", series);
        result.put("pendingMaintenances", pending.stream().map(mr -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", mr.getId());
            map.put("date", mr.getCreatedAt() == null ? null : mr.getCreatedAt().toLocalDate().toString());
            map.put("houseAddress", mr.getHouseId() == null ? null : mr.getHouseAddress());
            map.put("issue", mr.getDescription());
            return map;
        }).collect(Collectors.toList()));
        result.put("recentReviews", reviews.stream().map(rv -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", rv.getId());
            map.put("author", rv.getReviewer() == null ? null : rv.getReviewer().getRealName());
            map.put("content", rv.getComment());
            map.put("date", rv.getCreatedAt() == null ? null : rv.getCreatedAt().toLocalDate().toString());
            return map;
        }).collect(Collectors.toList()));

        return ResponseEntity.ok(result);
    }
}
