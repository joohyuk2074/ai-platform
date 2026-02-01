package me.joohyuk.commonsaga;

import com.spartaecommerce.domain.event.DomainEvent;

public interface SagaStep<T, S extends DomainEvent, U extends DomainEvent> {

  S process(T data);

  U rollback(T data);
}
