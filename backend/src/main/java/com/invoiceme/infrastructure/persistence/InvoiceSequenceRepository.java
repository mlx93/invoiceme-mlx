package com.invoiceme.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Repository for managing invoice sequence numbers.
 * Uses a simple table to track the current sequence number per year.
 */
@Repository
public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, Integer> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InvoiceSequence s WHERE s.year = :year")
    Optional<InvoiceSequence> findByYearForUpdate(@Param("year") int year);
    
    Optional<InvoiceSequence> findByYear(int year);
}

