package me.joohyuk.datarex.domain.entity;

import java.time.Instant;

/**
 * Document Transform 완료 메시지.
 *
 * <p>문서 청킹 작업이 성공적으로 완료되었을 때 발행되는 메시지입니다.
 */
public record DocumentTransformCompletedMessage(
    String eventId,
    String collectionId,
    String documentId,
    String passageVersion,
    int passageCount,
    Instant occurredAt
) {

}