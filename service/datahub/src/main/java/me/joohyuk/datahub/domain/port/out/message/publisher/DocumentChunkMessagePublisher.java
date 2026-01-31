package me.joohyuk.datahub.domain.port.out.message.publisher;

import com.spartaecommerce.domain.event.publisher.DomainEventPublisher;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;

public interface DocumentChunkMessagePublisher
    extends DomainEventPublisher<DocumentUploadedEvent> {

}
