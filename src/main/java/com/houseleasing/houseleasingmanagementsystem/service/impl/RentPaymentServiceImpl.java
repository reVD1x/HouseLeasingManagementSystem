package com.houseleasing.houseleasingmanagementsystem.service.impl;

import com.houseleasing.houseleasingmanagementsystem.model.Contract;
import com.houseleasing.houseleasingmanagementsystem.model.RentPayment;
import com.houseleasing.houseleasingmanagementsystem.model.enums.PaymentCycle;
import com.houseleasing.houseleasingmanagementsystem.model.enums.PaymentStatus;
import com.houseleasing.houseleasingmanagementsystem.repository.ContractRepository;
import com.houseleasing.houseleasingmanagementsystem.repository.RentPaymentRepository;
import com.houseleasing.houseleasingmanagementsystem.service.RentPaymentService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class RentPaymentServiceImpl implements RentPaymentService {

    @Autowired
    private RentPaymentRepository rentPaymentRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Override
    @Transactional
    public RentPayment create(RentPayment rentPayment) {
        // 必须关联合同
        if (rentPayment.getContract() == null || rentPayment.getContract().getId() == null) {
            throw new IllegalArgumentException("contract id 必须提供");
        }
        Contract contract = contractRepository.findById(rentPayment.getContract().getId()).orElse(null);
        if (contract == null) {
            throw new IllegalArgumentException("合同不存在: " + rentPayment.getContract().getId());
        }
        rentPayment.setContract(contract);
        // 默认状态、金额等落地逻辑
        if (rentPayment.getStatus() == null) {
            rentPayment.setStatus(PaymentStatus.PENDING);
        }
        if (rentPayment.getPeriodStart() == null && rentPayment.getDueDate() != null) {
            rentPayment.setPeriodStart(rentPayment.getDueDate().withDayOfMonth(1));
        }
        if (rentPayment.getPeriodEnd() == null && rentPayment.getPeriodStart() != null) {
            rentPayment.setPeriodEnd(rentPayment.getPeriodStart().plusMonths(1).minusDays(1));
        }
        RentPayment saved = rentPaymentRepository.save(rentPayment);
        return getById(saved.getId());
    }

    @Override
    @Transactional
    public RentPayment update(Long id, RentPayment rentPayment) {
        RentPayment rp = getById(id);
        if (rp == null) return null;
        // 更新可修改字段
        rp.setDueDate(rentPayment.getDueDate());
        rp.setAmount(rentPayment.getAmount());
        rp.setPaidAt(rentPayment.getPaidAt());
        rp.setStatus(rentPayment.getStatus());
        rp.setPeriodStart(rentPayment.getPeriodStart());
        rp.setPeriodEnd(rentPayment.getPeriodEnd());
        rp.setPenalty(rentPayment.getPenalty());
        rentPaymentRepository.save(rp);
        return getById(rp.getId());
    }

    @Override
    public void delete(Long id) {
        rentPaymentRepository.deleteById(id);
    }

    @Override
    public RentPayment getById(Long id) {
        return rentPaymentRepository.findByIdWithRelations(id).orElse(null);
    }

    @Override
    public Page<RentPayment> search(Long contractId,
                                    PaymentStatus status,
                                    LocalDate dueFrom,
                                    LocalDate dueTo,
                                    Pageable pageable) {
        Specification<RentPayment> spec = (root, query, cb) -> {
            if (RentPayment.class.equals(query.getResultType())) {
                var cFetch = root.fetch("contract", JoinType.LEFT);
                // fetch nested associations to support transient getters safely
                try {
                    var contractJoin = (Join<?, ?>) cFetch;
                    contractJoin.fetch("house", JoinType.LEFT);
                    contractJoin.fetch("landlord", JoinType.LEFT);
                    contractJoin.fetch("tenant", JoinType.LEFT);
                } catch (ClassCastException ignored) { /* for count queries */ }
            }
            List<Predicate> ps = new ArrayList<>();
            if (contractId != null) {
                ps.add(cb.equal(root.get("contract").get("id"), contractId));
            }
            if (status != null) {
                ps.add(cb.equal(root.get("status"), status));
            }
            if (dueFrom != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("dueDate"), dueFrom));
            }
            if (dueTo != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueTo));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        Page<RentPayment> page = rentPaymentRepository.findAll(spec, pageable);
        page.getContent().forEach(rp -> {
            if (rp.getStatus() == PaymentStatus.PENDING && rp.getDueDate() != null && rp.getDueDate().isBefore(LocalDate.now())) {
                rp.setStatus(PaymentStatus.OVERDUE);
            }
        });
        return page;
    }

    @Override
    public List<RentPayment> listByDueDateBetween(LocalDate from, LocalDate to) {
        return rentPaymentRepository.findByDueDateBetween(from, to);
    }

    @Override
    public List<RentPayment> listOverduePending(LocalDate beforeDate) {
        // 返回 dueDate 在 beforeDate 之前，状态为 PENDING 或 OVERDUE 的记录
        return rentPaymentRepository.findOverdueBefore(beforeDate, Arrays.asList(PaymentStatus.PENDING, PaymentStatus.OVERDUE));
    }

    @Override
    @Transactional
    public RentPayment markReminded(Long id) {
        RentPayment rp = rentPaymentRepository.findById(id).orElse(null);
        if (rp == null) return null;
        rp.setRemindedAt(LocalDateTime.now());
        rentPaymentRepository.save(rp);
        return getById(rp.getId());
    }

    @Override
    @Transactional
    public RentPayment markOverdueProcessed(Long id, Double penalty) {
        RentPayment rp = rentPaymentRepository.findById(id).orElse(null);
        if (rp == null) return null;
        rp.setOverdueProcessed(true);
        rp.setPenalty(penalty);
        // 设置逾期即将状态持久化为 OVERDUE（除非已经支付）
        if (rp.getStatus() != null && rp.getStatus() != PaymentStatus.PAID) {
            rp.setStatus(PaymentStatus.OVERDUE);
        }
        rentPaymentRepository.save(rp);
        return getById(rp.getId());
    }

    @Override
    @Transactional
    public RentPayment markPaid(Long id) {
        RentPayment rp = rentPaymentRepository.findById(id).orElse(null);
        if (rp == null) return null;
        rp.setStatus(PaymentStatus.PAID);
        rp.setPaidAt(LocalDateTime.now());
        rentPaymentRepository.save(rp);
        return getById(rp.getId());
    }

    @Override
    @Transactional
    public void generateScheduleForContract(Contract contract, boolean markFirstPaid) {
        if (contract == null || contract.getId() == null) return;
        // 防止重复生成：若已存在租金记录则跳过生成
        if (rentPaymentRepository.existsByContract_Id(contract.getId())) {
            // already generated; do nothing
            return;
        }
        LocalDate cursor = contract.getStartDate();
        LocalDate end = contract.getEndDate();
        PaymentCycle cycle = contract.getPaymentCycle();
        if (cycle == null) cycle = PaymentCycle.MONTHLY;
        int stepMonths;
        switch (cycle) {
            case MONTHLY:
                stepMonths = 1;
                break;
            case QUARTERLY:
                stepMonths = 3;
                break;
            case HALF_YEARLY:
                stepMonths = 6;
                break;
            case YEARLY:
                stepMonths = 12;
                break;
            default:
                stepMonths = 1;
                break;
        }
        boolean first = true;
        while (!cursor.isAfter(end)) {
            LocalDate periodStart = cursor.withDayOfMonth(1);
            LocalDate dueDate = cursor; // 可以根据业务改为 periodStart 或期末
            LocalDate periodEnd = periodStart.plusMonths(stepMonths).minusDays(1);
            if (periodEnd.isAfter(end)) periodEnd = end;

            // Skip creation if a rent payment with same contract and dueDate already exists
            if (rentPaymentRepository.existsByContract_IdAndDueDate(contract.getId(), dueDate)) {
                cursor = periodStart.plusMonths(stepMonths);
                first = false;
                continue;
            }

            RentPayment rp = new RentPayment();
            rp.setContract(contract);
            rp.setDueDate(dueDate);
            rp.setAmount(contract.getRentAmount());
            rp.setStatus(first && markFirstPaid ? PaymentStatus.PAID : PaymentStatus.PENDING);
            if (first && markFirstPaid) {
                rp.setPaidAt(LocalDateTime.now());
            }
            rp.setPeriodStart(periodStart);
            rp.setPeriodEnd(periodEnd);
            rp.setOverdueProcessed(false);
            rentPaymentRepository.save(rp);

            cursor = periodStart.plusMonths(stepMonths);
            first = false;
        }
    }
}
