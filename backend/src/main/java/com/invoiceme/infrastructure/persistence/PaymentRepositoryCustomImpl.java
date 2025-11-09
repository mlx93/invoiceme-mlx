package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.PaymentMethod;
import com.invoiceme.domain.common.PaymentStatus;
import com.invoiceme.domain.payment.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Page<Payment> findByFilters(
        UUID invoiceId,
        UUID customerId,
        LocalDate paymentDateFrom,
        LocalDate paymentDateTo,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        Pageable pageable
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // Query for results
        CriteriaQuery<Payment> query = cb.createQuery(Payment.class);
        Root<Payment> payment = query.from(Payment.class);
        
        // Build predicates
        List<Predicate> predicates = buildPredicates(cb, payment, invoiceId, customerId, 
            paymentDateFrom, paymentDateTo, paymentMethod, status);
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(payment.get("createdAt")));
        
        // Execute query with pagination
        List<Payment> results = entityManager.createQuery(query)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();
        
        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Payment> countRoot = countQuery.from(Payment.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, invoiceId, customerId,
            paymentDateFrom, paymentDateTo, paymentMethod, status);
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        return new PageImpl<>(results, pageable, total);
    }
    
    private List<Predicate> buildPredicates(
        CriteriaBuilder cb,
        Root<Payment> payment,
        UUID invoiceId,
        UUID customerId,
        LocalDate paymentDateFrom,
        LocalDate paymentDateTo,
        PaymentMethod paymentMethod,
        PaymentStatus status
    ) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (invoiceId != null) {
            predicates.add(cb.equal(payment.get("invoiceId"), invoiceId));
        }
        
        if (customerId != null) {
            predicates.add(cb.equal(payment.get("customerId"), customerId));
        }
        
        if (paymentDateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(payment.get("paymentDate"), paymentDateFrom));
        }
        
        if (paymentDateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(payment.get("paymentDate"), paymentDateTo));
        }
        
        if (paymentMethod != null) {
            predicates.add(cb.equal(payment.get("paymentMethod"), paymentMethod));
        }
        
        if (status != null) {
            predicates.add(cb.equal(payment.get("status"), status));
        }
        
        return predicates;
    }
}

