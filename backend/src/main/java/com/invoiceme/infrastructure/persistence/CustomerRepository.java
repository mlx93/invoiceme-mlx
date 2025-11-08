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
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    
    Optional<Customer> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);
    
    Page<Customer> findByCustomerType(CustomerType customerType, Pageable pageable);
    
    @Query("SELECT c FROM Customer c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:customerType IS NULL OR c.customerType = :customerType) AND " +
           "(:search IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email.value) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> findByFilters(
        @Param("status") CustomerStatus status,
        @Param("customerType") CustomerType customerType,
        @Param("search") String search,
        Pageable pageable
    );
    
    @Query("SELECT c FROM Customer c WHERE c.status = 'ACTIVE' AND " +
           "EXISTS (SELECT 1 FROM Invoice i WHERE i.customerId = c.id AND i.status IN ('SENT', 'OVERDUE') AND i.balanceDue.amount > 0)")
    Page<Customer> findCustomersWithOutstandingBalance(Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.status = :status")
    long countByStatus(@Param("status") CustomerStatus status);
}

