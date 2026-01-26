package me.joohyuk.ingestion.domain.entity;

import com.spartaecommerce.domain.entity.AggregateRoot;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import me.joohyuk.ingestion.domain.vo.Content;
import me.joohyuk.ingestion.domain.vo.DocumentId;
import me.joohyuk.ingestion.domain.vo.DocumentStatus;
import me.joohyuk.ingestion.domain.vo.Metadata;

@Getter
public class Document extends AggregateRoot<DocumentId> {

  private final Content content;
  private final Metadata metadata;
  private DocumentStatus status;
  private final Instant createdAt;
  private Instant updatedAt;
  private String validationMessage;

  private Document(
      DocumentId id,
      Content content,
      Metadata metadata,
      DocumentStatus status,
      Instant createdAt,
      Instant updatedAt,
      String validationMessage
  ) {
    super.setId(Objects.requireNonNull(id, "Document ID cannot be null"));
    this.content = Objects.requireNonNull(content, "Content cannot be null");
    this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
    this.status = Objects.requireNonNull(status, "Status cannot be null");
    this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
    this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
    this.validationMessage = validationMessage;
  }

  /**
   * 새로운 문서 생성 (업로드 시점)
   */
  public static Document create(Content content, Metadata metadata) {
    Instant now = Instant.now();
    return new Document(
        DocumentId.generate(),
        content,
        metadata,
        DocumentStatus.UPLOADED,
        now,
        now,
        null
    );
  }

  /**
   * 기존 문서 재구성 (저장소에서 로드 시)
   */
  public static Document reconstruct(
      DocumentId id,
      Content content,
      Metadata metadata,
      DocumentStatus status,
      Instant createdAt,
      Instant updatedAt,
      String validationMessage
  ) {
    return new Document(id, content, metadata, status, createdAt, updatedAt, validationMessage);
  }

  /**
   * 검수 시작
   */
  public void startValidation() {
    if (!status.isProcessable()) {
      throw new IllegalStateException(
          "Document cannot be validated in current status: " + status
      );
    }
    this.status = DocumentStatus.VALIDATING;
    this.updatedAt = Instant.now();
  }

  /**
   * 검수 성공
   */
  public void markAsValidated() {
    if (status != DocumentStatus.VALIDATING) {
      throw new IllegalStateException(
          "Document must be in VALIDATING status to mark as validated"
      );
    }
    this.status = DocumentStatus.VALIDATED;
    this.validationMessage = null;
    this.updatedAt = Instant.now();
  }

  /**
   * 검수 실패
   */
  public void markAsValidationFailed(String reason) {
    if (status != DocumentStatus.VALIDATING) {
      throw new IllegalStateException(
          "Document must be in VALIDATING status to mark as failed"
      );
    }
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("Validation failure reason cannot be empty");
    }
    this.status = DocumentStatus.VALIDATION_FAILED;
    this.validationMessage = reason;
    this.updatedAt = Instant.now();
  }

  /**
   * 청킹 완료 처리
   */
  public void markAsChunked() {
    if (status != DocumentStatus.VALIDATED) {
      throw new IllegalStateException(
          "Only validated documents can be marked as chunked"
      );
    }
    this.status = DocumentStatus.CHUNKED;
    this.updatedAt = Instant.now();
  }

  /**
   * Passage 생성 완료 처리
   */
  public void markAsPassagesGenerated() {
    if (status != DocumentStatus.CHUNKED) {
      throw new IllegalStateException(
          "Only chunked documents can have passages generated"
      );
    }
    this.status = DocumentStatus.PASSAGES_GENERATED;
    this.updatedAt = Instant.now();
  }

  /**
   * 임베딩 완료 처리
   */
  public void markAsEmbedded() {
    if (status != DocumentStatus.PASSAGES_GENERATED) {
      throw new IllegalStateException(
          "Only documents with generated passages can be marked as embedded"
      );
    }
    this.status = DocumentStatus.EMBEDDED;
    this.updatedAt = Instant.now();
  }

  /**
   * 처리 실패 처리
   */
  public void markAsFailed(String reason) {
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("Failure reason cannot be empty");
    }
    this.status = DocumentStatus.FAILED;
    this.validationMessage = reason;
    this.updatedAt = Instant.now();
  }

  // Getters
  public DocumentId getId() {
    return super.getId();
  }
}
