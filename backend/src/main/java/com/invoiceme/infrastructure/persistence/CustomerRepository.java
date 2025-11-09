package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.CustomerStatus;
import com.invoiceme.domain.common.CustomerType;
import com.invoiceme.domain.customer.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID>, CustomerRepositoryCustom {
    
    @Query("SELECT c FROM Customer c WHERE c.email.value = :email")
    Optional<Customer> findByEmail(@Param("email") String email);
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c WHERE c.email.value = :email")
    boolean existsByEmail(@Param("email") String email);
    
    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);
    
    Page<Customer> findByCustomerType(CustomerType customerType, Pageable pageable);
    
    Page<Customer> findByFilters(
        CustomerStatus status,
        CustomerType customerType,
        String search,
        Pageable pageable
    );
    
    @Query("SELECT c FROM Customer c WHERE c.status = 'ACTIVE' AND " +
           "EXISTS (SELECT 1 FROM Invoice i WHERE i.customerId = c.id AND i.status IN ('SENT', 'OVERDUE') AND i.balanceDue.amount > 0)")
    Page<Customer> findCustomersWithOutstandingBalance(Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.status = :status")
    long countByStatus(@Param("status") CustomerStatus status);
}

