package com.invoiceme.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activity_feed")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityFeed {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
    
    @Column(name = "user_id")
    private UUID userId;
    
    public static ActivityFeed create(UUID aggregateId, String eventType, String description, UUID userId) {
        ActivityFeed entry = new ActivityFeed();
        entry.aggregateId = aggregateId;
        entry.eventType = eventType;
        entry.description = description;
        entry.occurredAt = Instant.now();
        entry.userId = userId;
        return entry;
    }
}

