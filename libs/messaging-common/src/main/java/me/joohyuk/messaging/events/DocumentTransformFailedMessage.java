package me.joohyuk.messaging.events;

import java.time.Instant;

/**
 * Document Transform 실패 메시지 (서비스 간 계약).
 *
 * <p>datarex 서비스가 문서 청킹 작업이 실패했을 때 발행하는 메시지입니다.
 * datahub 서비스는 이 메시지를 소비하여 Document 엔티티의 상태를 업데이트합니다.
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
