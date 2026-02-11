package me.joohyuk.datahub.fake;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import me.joohyuk.commonsaga.SagaStatus;
import me.joohyuk.datahub.application.port.out.message.publisher.TransformDocumentMessagePublisher;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;

/**
 * Fake TransformDocumentMessagePublisher 구현
 *
 * <p>Classical School 원칙에 따라 mock이 아닌 fake 구현을 사용합니다:
 * <ul>
 *   <li>실제 메시지 발행자처럼 동작하는 in-memory 구현</li>
 *   <li>Kafka 같은 외부 인프라 대신 메모리에 발행 기록 저장</li>
 *   <li>Callback 실행을 실제로 수행하여 통합 동작 검증</li>
 *   <li>발행 성공/실패 시나리오를 제어 가능</li>
 * </ul>
 *
 * <p>이 방식의 장점:
 * <ol>
 *   <li>외부 의존성 제거: Kafka 없이도 메시지 발행 로직 테스트 가능</li>
 *   <li>빠른 실행: In-memory 구현으로 빠른 테스트 피드백</li>
 *   <li>상태 기반 검증: 발행된 메시지 기록을 통한 직관적 검증</li>
 *   <li>시나리오 제어: 성공/실패 시나리오를 명시적으로 제어</li>
 *   <li>실제 동작 검증: Callback이 실제로 실행되어 통합 동작 확인</li>
 * </ol>
 *
 * <p>주의: 이것은 진정한 외부 경계(Kafka)를 대체하는 Fake입니다.
 * Classical School에서는 외부 인프라에 대해서만 Fake/Mock 사용을 허용합니다.
 */
public class FakeTransformDocumentMessagePublisher implements
    TransformDocumentMessagePublisher {

  private final List<PublishedMessage> publishedMessages = new ArrayList<>();
  private boolean shouldFail = false;
  private OutboxStatus statusToReturn = OutboxStatus.SENT;

  @Override
  public void publish(
      TransformDocumentOutbox transformDocumentOutbox,
      BiConsumer<TransformDocumentOutbox, OutboxStatus> outboxCallback
  ) {
    Objects.requireNonNull(transformDocumentOutbox, "TransformDocumentOutbox cannot be null");
    Objects.requireNonNull(outboxCallback, "Callback cannot be null");

    // Record the published message
    publishedMessages.add(new PublishedMessage(
        transformDocumentOutbox.getSagaId(),
        transformDocumentOutbox.getType(),
        transformDocumentOutbox.getPayload(),
        transformDocumentOutbox.getOutboxStatus(),
        transformDocumentOutbox.getSagaStatus()
    ));

    // Execute the callback immediately (simulating successful/failed publishing)
    if (shouldFail) {
      outboxCallback.accept(transformDocumentOutbox, OutboxStatus.FAILED);
    } else {
      outboxCallback.accept(transformDocumentOutbox, statusToReturn);
    }
  }

  // ─── Test Helpers ──────────────────────────────────────────────

  /**
   * 테스트 헬퍼: 발행된 메시지 수 반환
   */
  public int getPublishedMessageCount() {
    return publishedMessages.size();
  }

  /**
   * 테스트 헬퍼: 모든 발행 기록 조회
   */
  public List<PublishedMessage> getPublishedMessages() {
    return Collections.unmodifiableList(publishedMessages);
  }

  /**
   * 테스트 헬퍼: 특정 SagaId로 발행된 메시지 조회
   */
  public List<PublishedMessage> getPublishedMessagesBySagaId(Long sagaId) {
    return publishedMessages.stream()
        .filter(msg -> msg.sagaId().equals(sagaId))
        .toList();
  }

  /**
   * 테스트 헬퍼: 발행 실패 시나리오 설정
   */
  public void setShouldFail(boolean shouldFail) {
    this.shouldFail = shouldFail;
  }

  /**
   * 테스트 헬퍼: 성공 시 반환할 OutboxStatus 설정
   */
  public void setStatusToReturn(OutboxStatus status) {
    this.statusToReturn = Objects.requireNonNull(status, "Status cannot be null");
  }

  /**
   * 테스트 헬퍼: 발행 기록 초기화
   */
  public void clear() {
    publishedMessages.clear();
    shouldFail = false;
    statusToReturn = OutboxStatus.SENT;
  }

  /**
   * 발행된 메시지 기록
   */
  public record PublishedMessage(
      Long sagaId,
      String type,
      String payload,
      OutboxStatus initialOutboxStatus,
      SagaStatus sagaStatus
  ) {

  }
}
