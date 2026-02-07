package me.joohyuk.datahub.application.port.out.message.publisher;

import com.spartaecommerce.domain.event.publisher.DomainEventPublisher;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;

public interface PassageCreationRequestPublisher extends DomainEventPublisher<TransformDocumentEvent> {

}
