package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.InvoiceStatus;
import com.invoiceme.domain.invoice.Invoice;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class InvoiceRepositoryImpl implements InvoiceRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Page<Invoice> findByFilters(
        List<InvoiceStatus> statusList,
        UUID customerId,
        LocalDate issueDateFrom,
        LocalDate issueDateTo,
        LocalDate dueDateFrom,
        LocalDate dueDateTo,
        BigDecimal amountFrom,
        BigDecimal amountTo,
        String search,
        Pageable pageable
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        CriteriaQuery<Invoice> cq = cb.createQuery(Invoice.class);
        Root<Invoice> root = cq.from(Invoice.class);
        List<Predicate> predicates = buildPredicates(
            cb,
            root,
            statusList,
            customerId,
            issueDateFrom,
            issueDateTo,
            dueDateFrom,
            dueDateTo,
            amountFrom,
            amountTo,
            search
        );
        cq.where(predicates.toArray(new Predicate[0]));
        applySorting(cb, cq, root, pageable.getSort());
        
        TypedQuery<Invoice> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Invoice> content = query.getResultList();
        
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Invoice> countRoot = countQuery.from(Invoice.class);
        List<Predicate> countPredicates = buildPredicates(
            cb,
            countRoot,
            statusList,
            customerId,
            issueDateFrom,
            issueDateTo,
            dueDateFrom,
            dueDateTo,
            amountFrom,
            amountTo,
            search
        );
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        return new PageImpl<>(content, pageable, total);
    }
    
    private List<Predicate> buildPredicates(
        CriteriaBuilder cb,
        Root<Invoice> root,
        List<InvoiceStatus> statusList,
        UUID customerId,
        LocalDate issueDateFrom,
        LocalDate issueDateTo,
        LocalDate dueDateFrom,
        LocalDate dueDateTo,
        BigDecimal amountFrom,
        BigDecimal amountTo,
        String search
    ) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (!CollectionUtils.isEmpty(statusList)) {
            predicates.add(root.get("status").in(statusList));
        }
        if (customerId != null) {
            predicates.add(cb.equal(root.get("customerId"), customerId));
        }
        if (issueDateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("issueDate"), issueDateFrom));
        }
        if (issueDateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("issueDate"), issueDateTo));
        }
        if (dueDateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
        }
        if (dueDateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
        }
        if (amountFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("totalAmount").get("amount"), amountFrom));
        }
        if (amountTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("totalAmount").get("amount"), amountTo));
        }
        if (StringUtils.hasText(search)) {
            predicates.add(cb.like(
                cb.lower(root.get("invoiceNumber").get("value")),
                "%" + search.toLowerCase() + "%"
            ));
        }
        
        return predicates;
    }
    
    private void applySorting(
        CriteriaBuilder cb,
        CriteriaQuery<Invoice> cq,
        Root<Invoice> root,
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


