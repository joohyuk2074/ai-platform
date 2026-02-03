package me.joohyuk.datarex.domain.port.out.message;

import me.joohyuk.datarex.domain.entity.PassageCreationCompletedMessage;
import me.joohyuk.datarex.domain.entity.PassageCreationFailedMessage;

public interface PassageResultEventPublisher {

    void publishCompleted(PassageCreationCompletedMessage message);

    void publishFailed(PassageCreationFailedMessage message);
}
