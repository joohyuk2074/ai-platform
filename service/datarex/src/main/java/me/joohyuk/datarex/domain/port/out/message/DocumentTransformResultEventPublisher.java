package me.joohyuk.datarex.domain.port.out.message;

import me.joohyuk.datarex.domain.entity.DocumentTransformCompletedMessage;
import me.joohyuk.datarex.domain.entity.DocumentTransformFailedMessage;

public interface DocumentTransformResultEventPublisher {

    void publishCompleted(DocumentTransformCompletedMessage message);

    void publishFailed(DocumentTransformFailedMessage message);
}
