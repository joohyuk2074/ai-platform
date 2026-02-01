package me.joohyuk.datarex.infrastructure.adapter.out.message;

import lombok.RequiredArgsConstructor;
import me.joohyuk.datarex.domain.entity.PassageCreationCompletedMessage;
import me.joohyuk.datarex.domain.entity.PassageCreationFailedMessage;
import me.joohyuk.datarex.domain.port.out.PassageResultEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaPassageResultEventPublisher implements PassageResultEventPublisher {

    private static final String COMPLETED_TOPIC = "passage.creation.completed";
    private static final String FAILED_TOPIC = "passage.creation.failed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishCompleted(PassageCreationCompletedMessage message) {
        kafkaTemplate.send(COMPLETED_TOPIC, message.documentId(), message);
    }

    @Override
    public void publishFailed(PassageCreationFailedMessage message) {
        kafkaTemplate.send(FAILED_TOPIC, message.documentId(), message);
    }
}
