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

  /**
   * Document를 생성합니다. (기본 팩터리 메서드, 주로 테스트용)
   * ID와 timestamp가 없는 불완전한 상태로 생성되므로, 프로덕션 코드에서는 createForUpload()를 사용하세요.
   */
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

  /**
   * 업로드용 Document를 생성합니다. (프로덕션 코드용)
   * ID, timestamp, trackingId가 모두 설정된 완전한 상태로 생성됩니다.
   *
   * @param collectionId 문서가 속한 컬렉션 ID
   * @param fileKey 파일 저장소의 파일 키
   * @param contentHash 파일 콘텐츠의 해시값
   * @param metadata 문서 메타데이터
   * @param documentId 문서 ID (IdGenerator로 생성)
   * @param trackingId 추적 ID (IdGenerator로 생성)
   * @param now 현재 시간 (DateTimeHolder로 생성)
   * @return 완전히 초기화된 Document
   */
  public static Document createForUpload(
      CollectionId collectionId,
      String fileKey,
      ContentHash contentHash,
      Metadata metadata,
      DocumentId documentId,
      TrackingId trackingId,
      Instant now
  ) {
    validate(collectionId, fileKey, contentHash, metadata, DocumentStatus.UPLOADED);

    Document document = Document.builder()
        .collectionId(collectionId)
        .fileKey(fileKey)
        .contentHash(contentHash)
        .metadata(metadata)
        .trackingId(trackingId)
        .status(DocumentStatus.UPLOADED)
        .createdAt(now)
        .updatedAt(now)
        .build();

    document.setId(documentId);
    return document;
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

  /**
   * @deprecated createForUpload() 팩터리 메서드를 사용하세요.
   * 이 메서드는 하위 호환성을 위해 유지되지만, 새 코드에서는 사용하지 마세요.
   */
  @Deprecated
  public void upload(DocumentId documentId, Instant now) {
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
  public void completeTransform(int passageCount, String eventId, Instant now) {
    if (status != DocumentStatus.TRANSFORM_REQUESTED) {
      throw new DatahubDomainException(
          "Cannot complete transform. current=" + status + ", expected=TRANSFORM_REQUESTED"
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
  public void failTransform(
      String errorCode,
      String errorMessage,
      String eventId,
      Instant now
  ) {
    if (status != DocumentStatus.TRANSFORM_REQUESTED) {
      throw new DatahubDomainException(
          "Cannot fail transform. current=" + status + ", expected=TRANSFORM_REQUESTED"
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

  /**
   * {@code TRANSFORMED → EMBED_REQUESTED} 로 전이합니다. Transform이 완료되고 Embedding을 요청할 때 호출합니다.
   *
   * @throws DatahubDomainException 현재 상태가 TRANSFORMED가 아닌 경우
   */
  public void requestEmbed(Instant now) {
    if (status != DocumentStatus.TRANSFORMED) {
      throw new DatahubDomainException(
          "Cannot request embed. current=" + status + ", expected=TRANSFORMED"
              + " [documentId=" + getId() + "]",
          DatahubErrorCode.INVALID_DOCUMENT_STATE
      );
    }
    this.status = DocumentStatus.EMBED_REQUESTED;
    this.updatedAt = now;
  }

  /**
   * {@code EMBED_REQUESTED → EMBEDDED} 로 전이합니다. vecdash에서 Embedding 완료 이벤트를 수신하면 호출합니다.
   *
   * @param eventId 수신한 결과 이벤트의 ID (멱등성 체크용)
   * @throws DatahubDomainException 현재 상태가 EMBED_REQUESTED가 아닌 경우
   */
  public void completeEmbed(String eventId, Instant now) {
    if (status != DocumentStatus.EMBED_REQUESTED) {
      throw new DatahubDomainException(
          "Cannot complete embed. current=" + status + ", expected=EMBED_REQUESTED"
              + " [documentId=" + getId() + "]",
          DatahubErrorCode.INVALID_DOCUMENT_STATE
      );
    }
    this.status = DocumentStatus.EMBEDDED;
    this.lastResultEventId = eventId;
    this.updatedAt = now;
  }

  /**
   * {@code EMBED_REQUESTED → EMBED_FAILED} 로 전이합니다. vecdash에서 Embedding 실패 이벤트를 수신하면 호출합니다.
   * {@code attempt}를 1 증가시키고 에러 정보를 저장합니다.
   *
   * @param errorCode    실패 이벤트의 에러 코드
   * @param errorMessage 실패 이벤트의 에러 메시지 (500자 초과 시 절단)
   * @param eventId      수신한 결과 이벤트의 ID (멱등성 체크용)
   * @throws DatahubDomainException 현재 상태가 EMBED_REQUESTED가 아닌 경우
   */
  public void failEmbed(
      String errorCode,
      String errorMessage,
      String eventId,
      Instant now
  ) {
    if (status != DocumentStatus.EMBED_REQUESTED) {
      throw new DatahubDomainException(
          "Cannot fail embed. current=" + status + ", expected=EMBED_REQUESTED"
              + " [documentId=" + getId() + "]",
          DatahubErrorCode.INVALID_DOCUMENT_STATE
      );
    }
    this.status = DocumentStatus.EMBED_FAILED;
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
