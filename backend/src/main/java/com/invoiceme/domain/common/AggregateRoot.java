package com.invoiceme.domain.common;

import jakarta.persistence.Transient;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public abstract class AggregateRoot {
    
    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    public void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }
    
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        domainEvents.clear();
    }
    
    public abstract UUID getId();
}

