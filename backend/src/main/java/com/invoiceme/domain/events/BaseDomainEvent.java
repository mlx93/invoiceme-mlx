package com.invoiceme.domain.events;

import com.invoiceme.domain.common.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class BaseDomainEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final String eventType;
    
    protected BaseDomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.eventType = this.getClass().getSimpleName();
    }
}

