package me.joohyuk.datarex.infrastructure.adapter.out.message;

import lombok.RequiredArgsConstructor;
import me.joohyuk.datarex.domain.entity.DocumentTransformCompletedMessage;
import me.joohyuk.datarex.domain.entity.DocumentTransformFailedMessage;
import me.joohyuk.datarex.domain.port.out.message.DocumentTransformResultEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaDocumentTransformResultEventPublisher implements DocumentTransformResultEventPublisher {

    private static final String COMPLETED_TOPIC = "document.transform.completed";
    private static final String FAILED_TOPIC = "document.transform.failed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishCompleted(DocumentTransformCompletedMessage message) {
        kafkaTemplate.send(COMPLETED_TOPIC, message.documentId(), message);
    }

    @Override
    public void publishFailed(DocumentTransformFailedMessage message) {
        kafkaTemplate.send(FAILED_TOPIC, message.documentId(), message);
    }
}
