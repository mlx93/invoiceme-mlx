package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.TemplateStatus;
import com.invoiceme.domain.recurring.RecurringInvoiceTemplate;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class RecurringInvoiceTemplateRepositoryImpl implements RecurringInvoiceTemplateRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Page<RecurringInvoiceTemplate> findByFilters(
        UUID customerId,
        TemplateStatus status,
        Pageable pageable
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        CriteriaQuery<RecurringInvoiceTemplate> cq = cb.createQuery(RecurringInvoiceTemplate.class);
        Root<RecurringInvoiceTemplate> root = cq.from(RecurringInvoiceTemplate.class);
        List<Predicate> predicates = buildPredicates(cb, root, customerId, status);
        cq.where(predicates.toArray(new Predicate[0]));
        applySorting(cb, cq, root, pageable.getSort());
        
        TypedQuery<RecurringInvoiceTemplate> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<RecurringInvoiceTemplate> content = query.getResultList();
        
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<RecurringInvoiceTemplate> countRoot = countQuery.from(RecurringInvoiceTemplate.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, customerId, status);
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        return new PageImpl<>(content, pageable, total);
    }
    
    private List<Predicate> buildPredicates(
        CriteriaBuilder cb,
        Root<RecurringInvoiceTemplate> root,
        UUID customerId,
        TemplateStatus status
    ) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (customerId != null) {
            predicates.add(cb.equal(root.get("customerId"), customerId));
        }
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        
        return predicates;
    }
    
    private void applySorting(
        CriteriaBuilder cb,
        CriteriaQuery<RecurringInvoiceTemplate> cq,
        Root<RecurringInvoiceTemplate> root,
        Sort sort
    ) {
        Sort effectiveSort = (sort == null || sort.isUnsorted())
            ? Sort.by(Sort.Direction.DESC, "createdAt")
            : sort;
        
        List<Order> orders = new ArrayList<>();
        for (Sort.Order order : effectiveSort) {
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


