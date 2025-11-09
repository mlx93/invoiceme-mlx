package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.PaymentMethod;
import com.invoiceme.domain.common.PaymentStatus;
import com.invoiceme.domain.payment.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {
    
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
        
        CriteriaQuery<Payment> cq = cb.createQuery(Payment.class);
        Root<Payment> root = cq.from(Payment.class);
        List<Predicate> predicates = buildPredicates(
            cb,
            root,
            invoiceId,
            customerId,
            paymentDateFrom,
            paymentDateTo,
            paymentMethod,
            status
        );
        cq.where(predicates.toArray(new Predicate[0]));
        applySorting(cb, cq, root, pageable.getSort());
        
        TypedQuery<Payment> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Payment> content = query.getResultList();
        
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Payment> countRoot = countQuery.from(Payment.class);
        List<Predicate> countPredicates = buildPredicates(
            cb,
            countRoot,
            invoiceId,
            customerId,
            paymentDateFrom,
            paymentDateTo,
            paymentMethod,
            status
        );
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        return new PageImpl<>(content, pageable, total);
    }
    
    private List<Predicate> buildPredicates(
        CriteriaBuilder cb,
        Root<Payment> root,
        UUID invoiceId,
        UUID customerId,
        LocalDate paymentDateFrom,
        LocalDate paymentDateTo,
        PaymentMethod paymentMethod,
        PaymentStatus status
    ) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (invoiceId != null) {
            predicates.add(cb.equal(root.get("invoiceId"), invoiceId));
        }
        if (customerId != null) {
            predicates.add(cb.equal(root.get("customerId"), customerId));
        }
        if (paymentDateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"), paymentDateFrom));
        }
        if (paymentDateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("paymentDate"), paymentDateTo));
        }
        if (paymentMethod != null) {
            predicates.add(cb.equal(root.get("paymentMethod"), paymentMethod));
        }
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        
        return predicates;
    }
    
    private void applySorting(
        CriteriaBuilder cb,
        CriteriaQuery<Payment> cq,
        Root<Payment> root,
        Sort sort
    ) {
        if (sort == null || sort.isUnsorted()) {
            return;
        }
        
        List<Order> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            jakarta.persistence.criteria.Path<?> path = root;
            for (String property : order.getProperty().split("\\.")) {
                path = path.get(property);
            }
            orders.add(order.isAscending() ? cb.asc(path) : cb.desc(path));
        }
        
        if (!orders.isEmpty()) {
            cq.orderBy(orders);
        }
    }
}


