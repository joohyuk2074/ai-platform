package me.joohyuk.datarex.domain.entity;

import java.time.Instant;

public record PassageCreationCompletedMessage(
    String eventId,
    String collectionId,
    String documentId,
    String passageVersion,
    int passageCount,
    Instant occurredAt
) {

}
