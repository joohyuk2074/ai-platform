package me.joohyuk.datahub.domain.entity;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * Document Transform 결과를 나타내는 응답 객체.
 *
 * <p>datarex 서비스에서 발행한 Transform 완료/실패 메시지를 수신하여
 * Document 엔티티의 상태를 변경하는 데 사용됩니다.
 */
@Getter
@Builder
public class PassageResponse {

  private String eventId;
  private Long collectionId;
  private Long documentId;
  private String passageVersion;
  private Integer passageCount;
  private String errorCode;
  private String errorMessage;
  private boolean success;
  private Instant occurredAt;

  public List<String> getFailureMessages() {
    if (success) {
      return List.of();
    }
    return List.of(String.format("[%s] %s", errorCode, errorMessage));
  }
}
