package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.CustomerStatus;
import com.invoiceme.domain.common.CustomerType;
import com.invoiceme.domain.customer.Customer;
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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Page<Customer> findByFilters(
        CustomerStatus status,
        CustomerType customerType,
        String search,
        Pageable pageable
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        CriteriaQuery<Customer> cq = cb.createQuery(Customer.class);
        Root<Customer> root = cq.from(Customer.class);
        List<Predicate> predicates = buildPredicates(cb, root, status, customerType, search);
        cq.where(predicates.toArray(new Predicate[0]));
        applySorting(cb, cq, root, pageable.getSort());
        
        TypedQuery<Customer> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Customer> content = query.getResultList();
        
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Customer> countRoot = countQuery.from(Customer.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, status, customerType, search);
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        return new PageImpl<>(content, pageable, total);
    }
    
    private List<Predicate> buildPredicates(
        CriteriaBuilder cb,
        Root<Customer> root,
        CustomerStatus status,
        CustomerType customerType,
        String search
    ) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (customerType != null) {
            predicates.add(cb.equal(root.get("customerType"), customerType));
        }
        if (StringUtils.hasText(search)) {
            String pattern = "%" + search.toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("companyName")), pattern),
                cb.like(cb.lower(root.get("email").get("value")), pattern)
            ));
        }
        
        return predicates;
    }
    
    private void applySorting(
        CriteriaBuilder cb,
        CriteriaQuery<Customer> cq,
        Root<Customer> root,
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


