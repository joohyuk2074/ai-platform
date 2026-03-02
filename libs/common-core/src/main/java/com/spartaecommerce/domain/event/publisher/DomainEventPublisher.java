package com.spartaecommerce.domain.event.publisher;

import com.spartaecommerce.domain.event.DomainEvent;

public interface DomainEventPublisher<T extends DomainEvent> {

  void publish(T domainEvent);
}
