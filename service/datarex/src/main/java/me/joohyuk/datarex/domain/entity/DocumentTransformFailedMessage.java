package me.joohyuk.datarex.domain.entity;

import java.time.Instant;

/**
 * Document Transform 실패 메시지.
 *
 * <p>문서 청킹 작업이 실패했을 때 발행되는 메시지입니다.
 */
public record DocumentTransformFailedMessage(
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
