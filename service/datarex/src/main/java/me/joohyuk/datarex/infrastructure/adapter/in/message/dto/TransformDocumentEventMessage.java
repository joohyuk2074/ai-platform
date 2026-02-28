package me.joohyuk.datarex.infrastructure.adapter.in.message.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spartaecommerce.domain.vo.Metadata;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Kafka로부터 수신하는 문서 변환 이벤트 메시지
 * <p>
 * datahub의 TransformDocumentEvent와 동일한 구조를 가집니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransformDocumentEventMessage {

  private Long documentId;
  private Long collectionId;
  private String fileKey;
  private String contentHash;
  private Metadata metadata;
  private String trackingId;
  private String status;
  private int attempt;
  private String lastErrorCode;
  private String lastErrorMessage;
  private int passageCount;
  private String lastResultEventId;
  private Instant documentCreatedAt;
  private Instant documentUpdatedAt;
  private Instant eventCreatedAt;
  private Long uploadedBy;
}
