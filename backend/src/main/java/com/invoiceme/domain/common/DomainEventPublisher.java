package com.invoiceme.domain.common;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DomainEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    public void publishEvents(AggregateRoot aggregate) {
        aggregate.getDomainEvents().forEach(applicationEventPublisher::publishEvent);
        aggregate.clearDomainEvents();
    }
    
    public void publishEvent(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}

