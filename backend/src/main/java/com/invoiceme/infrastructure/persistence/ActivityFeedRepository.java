package com.invoiceme.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActivityFeedRepository extends JpaRepository<ActivityFeed, UUID> {
    Page<ActivityFeed> findByAggregateId(UUID aggregateId, Pageable pageable);
    Page<ActivityFeed> findByEventType(String eventType, Pageable pageable);
    Page<ActivityFeed> findByUserId(UUID userId, Pageable pageable);
}

