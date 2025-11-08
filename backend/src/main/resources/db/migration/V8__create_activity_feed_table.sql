-- V8: Create activity_feed table
-- This migration creates the activity_feed table for logging domain events and audit trail

-- Create activity_feed table
CREATE TABLE activity_feed (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id UUID,
    CONSTRAINT fk_activity_feed_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create index on occurred_at DESC for recent activity queries (most recent first)
CREATE INDEX idx_activity_feed_occurred_at ON activity_feed(occurred_at DESC);

-- Create index on aggregate_id for filtering by entity
CREATE INDEX idx_activity_feed_aggregate_id ON activity_feed(aggregate_id);

-- Create composite index for common query: aggregate_id + occurred_at DESC
CREATE INDEX idx_activity_feed_aggregate_date ON activity_feed(aggregate_id, occurred_at DESC);

-- Create index on event_type for filtering by event type
CREATE INDEX idx_activity_feed_event_type ON activity_feed(event_type);

