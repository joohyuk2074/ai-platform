package me.joohyuk.datahub.domain.entity;

import com.spartaecommerce.domain.entity.AggregateRoot;
import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.ContentHash;
import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.domain.vo.Metadata;
import com.spartaecommerce.domain.vo.TrackingId;
import com.spartaecommerce.domain.vo.UserId;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import me.joohyuk.datahub.domain.vo.DocumentStatus;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Document extends AggregateRoot<DocumentId> {

  private final CollectionId collectionId;

  /**
   * 파일 저장소에서 원본 파일의 위치를 가리키는 키 예: "documents/1234567890_sample.md"
   */
  private final String fileKey;

  /**
   * 파일 콘텐츠의 SHA-256 해시값. 중복 파일 검증에 사용됩니다.
   */
  private final ContentHash contentHash;

  private final Metadata metadata;

  private TrackingId trackingId;
  private DocumentStatus status;
  private int attempt;
  private String lastErrorCode;
  private String lastErrorMessage;
  private int passageCount;
  private String lastResultEventId;

  private Instant createdAt;
  private Instant updatedAt;

  public static Document create(
      CollectionId collectionId,
      String fileKey,
      ContentHash contentHash,
      Metadata metadata
  ) {
    validate(collectionId, fileKey, contentHash, metadata, DocumentStatus.UPLOADED);

    return Document.builder()
        .collectionId(collectionId)
        .fileKey(fileKey)
        .contentHash(contentHash)
        .metadata(metadata)
        .trackingId(new TrackingId(UUID.randomUUID()))
        .status(DocumentStatus.UPLOADED)
        .build();
  }

  public static Document restore(
      DocumentId id,
      CollectionId collectionId,
      String fileKey,
      ContentHash contentHash,
      Metadata metadata,
      TrackingId trackingId,
      DocumentStatus status,
      int attempt,
      String lastErrorCode,
      String lastErrorMessage,
      int passageCount,
      String lastResultEventId,
      Instant createdAt,
      Instant updatedAt
  ) {
    validate(collectionId, fileKey, contentHash, metadata, status);

    Document document = Document.builder()
        .collectionId(collectionId)
        .fileKey(fileKey)
        .contentHash(contentHash)
        .metadata(metadata)
        .trackingId(trackingId)
        .status(status)
        .attempt(attempt)
        .lastErrorCode(lastErrorCode)
        .lastErrorMessage(lastErrorMessage)
        .passageCount(passageCount)
        .lastResultEventId(lastResultEventId)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .build();

    document.setId(id);

    return document;
  }

  public void initialize(DocumentId documentId, Instant now) {
    super.setId(documentId);
    this.createdAt = now;
    this.updatedAt = now;
  }

  public DocumentId getId() {
    return super.getId();
  }

  // ─── 상태 전이 메서드 ────────────────────────────────────────────

  /**
   * {@code UPLOADED → TRANSFORM_REQUESTED} 로 전이합니다. Kafka 이벤트 publish 직전에 호출합니다.
   *
   * @throws DatahubDomainException 현재 상태가 UPLOADED가 아닌 경우
   */
  public void transform(Instant now) {
    if (status != DocumentStatus.UPLOADED) {
      throw new DatahubDomainException(
          "Cannot request transform. current=" + status + ", expected=UPLOADED"
              + " [documentId=" + getId() + "]",
          DatahubErrorCode.INVALID_DOCUMENT_STATE
      );
    }
    this.status = DocumentStatus.TRANSFORM_REQUESTED;
    this.updatedAt = now;
  }

  /**
   * {@code TRANSFORM_REQUESTED → TRANSFORMED} 로 전이합니다. datarex에서 Transform 완료 이벤트를 수신하면 호출합니다.
   *
   * @param passageCount 생성된 청크 수
   * @param eventId      수신한 결과 이벤트의 ID (멱등성 체크용)
   * @throws DatahubDomainException 현재 상태가 TRANSFORM_REQUESTED가 아닌 경우
   */
  public void markTransformed(int passageCount, String eventId, Instant now) {
    if (status != DocumentStatus.TRANSFORM_REQUESTED) {
      throw new DatahubDomainException(
          "Cannot mark transformed. current=" + status + ", expected=TRANSFORM_REQUESTED"
              + " [documentId=" + getId() + "]",
          DatahubErrorCode.INVALID_DOCUMENT_STATE
      );
    }
    this.status = DocumentStatus.TRANSFORMED;
    this.passageCount = passageCount;
    this.lastResultEventId = eventId;
    this.updatedAt = now;
  }

  /**
   * {@code TRANSFORM_REQUESTED → TRANSFORM_FAILED} 로 전이합니다. datarex에서 Transform 실패 이벤트를 수신하면 호출합니다.
   * {@code attempt}를 1 증가시키고 에러 정보를 저장합니다.
   *
   * @param errorCode    실패 이벤트의 에러 코드
   * @param errorMessage 실패 이벤트의 에러 메시지 (500자 초과 시 절단)
   * @param eventId      수신한 결과 이벤트의 ID (멱등성 체크용)
   * @throws DatahubDomainException 현재 상태가 TRANSFORM_REQUESTED가 아닌 경우
   */
  public void markPassageFailed(
      String errorCode,
      String errorMessage,
      String eventId,
      Instant now
  ) {
    if (status != DocumentStatus.TRANSFORM_REQUESTED) {
      throw new DatahubDomainException(
          "Cannot mark transform failed. current=" + status + ", expected=TRANSFORM_REQUESTED"
              + " [documentId=" + getId() + "]",
          DatahubErrorCode.INVALID_DOCUMENT_STATE
      );
    }
    this.status = DocumentStatus.TRANSFORM_FAILED;
    this.attempt++;
    this.lastErrorCode = errorCode;
    this.lastErrorMessage = truncateErrorMessage(errorMessage);
    this.lastResultEventId = eventId;
    this.updatedAt = now;
  }

  public UserId getUploader() {
    return new UserId(this.metadata.uploadedBy());
  }

  // ─── private helpers ────────────────────────────────────────────

  private static final int MAX_ERROR_MESSAGE_LENGTH = 500;

  private static String truncateErrorMessage(String message) {
    if (message == null) {
      return null;
    }
    return message.length() > MAX_ERROR_MESSAGE_LENGTH
        ? message.substring(0, MAX_ERROR_MESSAGE_LENGTH)
        : message;
  }

  private static void validate(
      CollectionId collectionId,
      String fileKey,
      ContentHash contentHash,
      Metadata metadata,
      DocumentStatus status
  ) {
    if (collectionId == null) {
      throw new IllegalArgumentException("collectionId cannot be null");
    }

    if (fileKey == null || fileKey.isBlank()) {
      throw new IllegalArgumentException("File key cannot be empty");
    }

    if (contentHash == null) {
      throw new IllegalArgumentException("Content hash cannot be null");
    }

    if (metadata == null) {
      throw new IllegalArgumentException("Metadata cannot be null");
    }

    if (status == null) {
      throw new IllegalArgumentException("DocumentStatus cannot be null");
    }
  }
}
