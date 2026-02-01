package me.joohyuk.datarex.domain.entity;

import java.time.Instant;

public record PassageCreationFailedMessage(
    String eventId,
    String collectionId,
    String documentId,
    String passageVersion,
    String errorCode,
    String errorMessage,
    boolean retryable,
    Instant occurredAt
) {

}
