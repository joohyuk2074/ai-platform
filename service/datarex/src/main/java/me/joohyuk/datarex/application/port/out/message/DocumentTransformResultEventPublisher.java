package me.joohyuk.datarex.application.port.out.message;

import me.joohyuk.messaging.events.DocumentTransformCompletedMessage;
import me.joohyuk.messaging.events.DocumentTransformFailedMessage;

public interface DocumentTransformResultEventPublisher {

    void publishCompleted(DocumentTransformCompletedMessage message);

    void publishFailed(DocumentTransformFailedMessage message);
}
