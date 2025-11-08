package com.invoiceme.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity to track invoice sequence numbers per year.
 * Used to generate unique invoice numbers: INV-YYYY-####
 */
@Entity
@Table(name = "invoice_sequences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvoiceSequence {
    
    @Id
    @Column(name = "year", nullable = false)
    private Integer year;
    
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;
    
    public static InvoiceSequence create(int year) {
        InvoiceSequence seq = new InvoiceSequence();
        seq.year = year;
        seq.sequenceNumber = 1;
        return seq;
    }
    
    public int getNextSequence() {
        return sequenceNumber++;
    }
}

