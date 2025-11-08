package com.invoiceme.domain.events;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class CustomerDeactivatedEvent extends BaseDomainEvent {
    private final UUID customerId;
    private final String customerName;
    private final String reason;
    private final Instant deactivatedAt;
    
    public CustomerDeactivatedEvent(UUID customerId, String customerName, String reason) {
        super();
        this.customerId = customerId;
        this.customerName = customerName;
        this.reason = reason;
        this.deactivatedAt = Instant.now();
    }
}

