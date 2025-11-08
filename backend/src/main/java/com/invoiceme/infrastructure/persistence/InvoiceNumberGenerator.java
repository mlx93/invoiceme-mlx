package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.InvoiceNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class InvoiceNumberGenerator {
    
    private final InvoiceSequenceRepository sequenceRepository;
    
    @Transactional
    public InvoiceNumber generateNext() {
        int currentYear = Year.now().getValue();
        
        var sequenceOpt = sequenceRepository.findByYearForUpdate(currentYear);
        InvoiceSequence sequence;
        
        if (sequenceOpt.isPresent()) {
            sequence = sequenceOpt.get();
        } else {
            sequence = InvoiceSequence.create(currentYear);
            sequence = sequenceRepository.save(sequence);
        }
        
        int nextSequence = sequence.getNextSequence();
        sequenceRepository.save(sequence);
        
        return InvoiceNumber.generate(nextSequence);
    }
}

