package com.spartaecommerce.outbox;

public interface OutboxScheduler {
    void processOutboxMessage();
}
