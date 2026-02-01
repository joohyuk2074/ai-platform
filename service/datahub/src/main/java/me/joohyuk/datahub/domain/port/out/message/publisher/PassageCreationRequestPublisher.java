package me.joohyuk.datahub.domain.port.out.message.publisher;

import com.spartaecommerce.domain.event.publisher.DomainEventPublisher;
import me.joohyuk.datahub.domain.event.PassageCreationRequestEvent;

public interface PassageCreationRequestPublisher extends DomainEventPublisher<PassageCreationRequestEvent> {

}
