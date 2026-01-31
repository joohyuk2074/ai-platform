package me.joohyuk.datahub.domain.entity;

import com.spartaecommerce.domain.entity.AggregateRoot;
import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.domain.vo.Metadata;
import java.time.Instant;
import lombok.Getter;
import me.joohyuk.datahub.domain.exception.IngestionDomainException;
import me.joohyuk.datahub.domain.vo.CollectionId;
import me.joohyuk.datahub.domain.vo.ContentHash;
import me.joohyuk.datahub.domain.vo.DocumentStatus;

@Getter
public class Document extends AggregateRoot<DocumentId> {

  /**
   * 파일 저장소에서 원본 파일의 위치를 가리키는 키 예: "documents/1234567890_sample.md"
   */
  private final String fileKey;

  /**
   * 파일 콘텐츠의 SHA-256 해시값. 중복 파일 검증에 사용됩니다.
   */
  private final ContentHash contentHash;

  private final CollectionId collectionId;

  private final Metadata metadata;

  private DocumentStatus status;
  private int attempt;
  private String lastErrorCode;
  private String lastErrorMessage;
  private int passageCount;
  private String lastResultEventId;

  private Instant createdAt;
  private Instant updatedAt;

  private Document(
      CollectionId collectionId,
      String fileKey,
      ContentHash contentHash,
      Metadata metadata,
      DocumentStatus status
  ) {
    validate(collectionId, fileKey, contentHash, metadata, status);

    this.collectionId = collectionId;
    this.fileKey = fileKey;
    this.contentHash = contentHash;
    this.metadata = metadata;
    this.status = status;
    this.attempt = 0;
    this.passageCount = 0;
  }

  public static Document create(
      CollectionId collectionId,
      String fileKey,
      ContentHash contentHash,
      Metadata metadata
  ) {
    return new Document(collectionId, fileKey, contentHash, metadata, DocumentStatus.UPLOADED);
  }

  public static Document restore(
      DocumentId id,
      CollectionId collectionId,
      String fileKey,
      ContentHash contentHash,
      Metadata metadata,
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

    Document doc = new Document(collectionId, fileKey, contentHash, metadata, status);
    doc.setId(id);
    doc.attempt = attempt;
    doc.lastErrorCode = lastErrorCode;
    doc.lastErrorMessage = lastErrorMessage;
    doc.passageCount = passageCount;
    doc.lastResultEventId = lastResultEventId;
    doc.createdAt = createdAt;
    doc.updatedAt = updatedAt;

    return doc;
  }

  public void initialize(Long id, Instant now) {
    super.setId(new DocumentId(id));
    this.createdAt = now;
    this.updatedAt = now;
  }

  public DocumentId getId() {
    return super.getId();
  }

  // ─── 상태 전이 메서드 ────────────────────────────────────────────

  /**
   * {@code UPLOADED → PASSAGE_REQUESTED} 로 전이합니다. Kafka 이벤트 publish 직전에 호출합니다.
   *
   * @throws IngestionDomainException 현재 상태가 UPLOADED가 아닌 경우
   */
  public void requestPassageCreation(Instant now) {
    if (status != DocumentStatus.UPLOADED) {
      throw new IngestionDomainException(
          "Cannot request passage creation. current=" + status + ", expected=UPLOADED"
              + " [documentId=" + getId() + "]");
    }
    this.status = DocumentStatus.PASSAGE_REQUESTED;
    this.updatedAt = now;
  }

  /**
   * {@code PASSAGE_REQUESTED → PASSAGE_CREATED} 로 전이합니다. datarex에서 Passage 생성 완료 이벤트를 수신하면 호출합니다.
   *
   * @param passageCount 생성된 Passage 수
   * @param eventId      수신한 결과 이벤트의 ID (멱등성 체크용)
   * @throws IngestionDomainException 현재 상태가 PASSAGE_REQUESTED가 아닌 경우
   */
  public void markPassageCreated(int passageCount, String eventId, Instant now) {
    if (status != DocumentStatus.PASSAGE_REQUESTED) {
      throw new IngestionDomainException(
          "Cannot mark passage created. current=" + status + ", expected=PASSAGE_REQUESTED"
              + " [documentId=" + getId() + "]");
    }
    this.status = DocumentStatus.PASSAGE_CREATED;
    this.passageCount = passageCount;
    this.lastResultEventId = eventId;
    this.updatedAt = now;
  }

  /**
   * {@code PASSAGE_REQUESTED → PASSAGE_FAILED} 로 전이합니다. datarex에서 Passage 생성 실패 이벤트를 수신하면 호출합니다.
   * {@code attempt}를 1 증가시키고 에러 정보를 저장합니다.
   *
   * @param errorCode    실패 이벤트의 에러 코드
   * @param errorMessage 실패 이벤트의 에러 메시지 (500자 초과 시 절단)
   * @param eventId      수신한 결과 이벤트의 ID (멱등성 체크용)
   * @throws IngestionDomainException 현재 상태가 PASSAGE_REQUESTED가 아닌 경우
   */
  public void markPassageFailed(
      String errorCode,
      String errorMessage,
      String eventId,
      Instant now
  ) {
    if (status != DocumentStatus.PASSAGE_REQUESTED) {
      throw new IngestionDomainException(
          "Cannot mark passage failed. current=" + status + ", expected=PASSAGE_REQUESTED"
              + " [documentId=" + getId() + "]");
    }
    this.status = DocumentStatus.PASSAGE_FAILED;
    this.attempt++;
    this.lastErrorCode = errorCode;
    this.lastErrorMessage = truncateErrorMessage(errorMessage);
    this.lastResultEventId = eventId;
    this.updatedAt = now;
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
