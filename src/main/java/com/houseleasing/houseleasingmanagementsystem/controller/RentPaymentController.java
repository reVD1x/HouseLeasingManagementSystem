package com.houseleasing.houseleasingmanagementsystem.controller;

import com.houseleasing.houseleasingmanagementsystem.model.Contract;
import com.houseleasing.houseleasingmanagementsystem.model.RentPayment;
import com.houseleasing.houseleasingmanagementsystem.model.enums.PaymentStatus;
import com.houseleasing.houseleasingmanagementsystem.service.ContractService;
import com.houseleasing.houseleasingmanagementsystem.service.RentPaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rent-payments")
public class RentPaymentController {

    @Autowired
    private RentPaymentService rentPaymentService;

    @Autowired
    private ContractService contractService;

    // 创建租金支付记录
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RentPayment rentPayment) {
        if (rentPayment.getContract() == null || rentPayment.getContract().getId() == null) {
            return ResponseEntity.badRequest().body("contract id 必须提供");
        }
        Contract contract = contractService.getContractById(rentPayment.getContract().getId());
        if (contract == null) {
            return ResponseEntity.badRequest().body("合同不存在");
        }
        rentPayment.setContract(contract);
        RentPayment saved = rentPaymentService.create(rentPayment);
        return ResponseEntity.ok(saved);
    }

    // 更新
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody RentPayment rentPayment) {
        RentPayment updated = rentPaymentService.update(id, rentPayment);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // 标记已支付
    @PostMapping("/{id}/pay")
    public ResponseEntity<?> markPaid(@PathVariable Long id) {
        RentPayment rp = rentPaymentService.markPaid(id);
        return rp != null ? ResponseEntity.ok(rp) : ResponseEntity.notFound().build();
    }

    // 标记已提醒
    @PostMapping("/{id}/remind")
    public ResponseEntity<?> markReminded(@PathVariable Long id) {
        RentPayment rp = rentPaymentService.markReminded(id);
        return rp != null ? ResponseEntity.ok(rp) : ResponseEntity.notFound().build();
    }

    // 标记逾期处理并附加罚金
    @PostMapping("/{id}/overdue-process")
    public ResponseEntity<?> markOverdueProcessed(@PathVariable Long id,
                                                  @RequestParam(required = false) Double penalty) {
        RentPayment rp = rentPaymentService.markOverdueProcessed(id, penalty);
        return rp != null ? ResponseEntity.ok(rp) : ResponseEntity.notFound().build();
    }

    // 删除
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rentPaymentService.delete(id);
        return ResponseEntity.ok().build();
    }

    // 查询（分页）
    @GetMapping("/search")
    public ResponseEntity<Page<RentPayment>> search(@RequestParam(required = false) Long contractId,
                                                    @RequestParam(required = false) PaymentStatus status,
                                                    @RequestParam(required = false) LocalDate dueFrom,
                                                    @RequestParam(required = false) LocalDate dueTo,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RentPayment> res = rentPaymentService.search(contractId, status, dueFrom, dueTo, pageable);
        return ResponseEntity.ok(res);
    }

    // 报表：日期范围列表（不分页，前端自行统计）
    @GetMapping("/report")
    public ResponseEntity<List<RentPayment>> report(@RequestParam LocalDate from,
                                                    @RequestParam LocalDate to) {
        List<RentPayment> list = rentPaymentService.listByDueDateBetween(from, to);
        return ResponseEntity.ok(list);
    }

    // 逾期/即将逾期列表（不分页）
    @GetMapping("/overdue")
    public ResponseEntity<List<RentPayment>> overdue(@RequestParam(required = false) LocalDate beforeDate) {
        LocalDate target = beforeDate != null ? beforeDate : LocalDate.now();
        List<RentPayment> list = rentPaymentService.listOverduePending(target);
        return ResponseEntity.ok(list);
    }

    // 详情
    @GetMapping("/{id}")
    public ResponseEntity<RentPayment> getById(@PathVariable Long id) {
        RentPayment rp = rentPaymentService.getById(id);
        return rp != null ? ResponseEntity.ok(rp) : ResponseEntity.notFound().build();
    }
}

