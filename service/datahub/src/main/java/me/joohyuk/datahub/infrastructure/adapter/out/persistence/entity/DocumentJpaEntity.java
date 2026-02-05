package me.joohyuk.datahub.infrastructure.adapter.out.persistence.entity;

import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.domain.vo.Metadata;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.joohyuk.datahub.domain.entity.Document;
import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.ContentHash;
import me.joohyuk.datahub.domain.vo.DocumentStatus;
import me.joohyuk.datahub.infrastructure.adapter.out.persistence.converter.MetadataConverter;

@Getter
@Entity
@Table(
    name = "documents",
    indexes = {
        @Index(name = "idx_documents_collection_id", columnList = "collection_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_documents_content_hash", columnNames = {"content_hash"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentJpaEntity {

  @Id
  @Column(name = "id")
  private Long id;

  @Column(name = "file_key", nullable = false)
  private String fileKey;

  @Column(name = "content_hash", nullable = false)
  private String contentHash;

  @Column(name = "collection_id", nullable = false)
  private Long collectionId;

  @Column(name = "metadata", columnDefinition = "TEXT", nullable = false)
  @Convert(converter = MetadataConverter.class)
  private Metadata metadata;

  @Column(name = "document_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private DocumentStatus documentStatus;

  @Column(name = "attempt", nullable = false)
  private int attempt;

  @Column(name = "last_error_code")
  private String lastErrorCode;

  @Column(name = "last_error_message", length = 500)
  private String lastErrorMessage;

  @Column(name = "passage_count", nullable = false)
  private int passageCount;

  @Column(name = "last_result_event_id")
  private String lastResultEventId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public DocumentJpaEntity(
      Long id,
      Long collectionId,
      String fileKey,
      String contentHash,
      Metadata metadata,
      DocumentStatus documentStatus,
      int attempt,
      String lastErrorCode,
      String lastErrorMessage,
      int passageCount,
      String lastResultEventId,
      Instant createdAt,
      Instant updatedAt
  ) {
    this.id = id;
    this.collectionId = collectionId;
    this.fileKey = fileKey;
    this.contentHash = contentHash;
    this.metadata = metadata;
    this.documentStatus = documentStatus;
    this.attempt = attempt;
    this.lastErrorCode = lastErrorCode;
    this.lastErrorMessage = lastErrorMessage;
    this.passageCount = passageCount;
    this.lastResultEventId = lastResultEventId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static DocumentJpaEntity from(Document domain) {
    return new DocumentJpaEntity(
        domain.getId().getValue(),
        domain.getCollectionId().getValue(),
        domain.getFileKey(),
        domain.getContentHash().getValue(),
        domain.getMetadata(),
        domain.getStatus(),
        domain.getAttempt(),
        domain.getLastErrorCode(),
        domain.getLastErrorMessage(),
        domain.getPassageCount(),
        domain.getLastResultEventId(),
        domain.getCreatedAt(),
        domain.getUpdatedAt()
    );
  }

  public Document toDomain() {
    return Document.restore(
        new DocumentId(this.id),
        new CollectionId(this.collectionId),
        this.fileKey,
        ContentHash.of(this.contentHash),
        this.metadata,
        this.documentStatus,
        this.attempt,
        this.lastErrorCode,
        this.lastErrorMessage,
        this.passageCount,
        this.lastResultEventId,
        this.createdAt,
        this.updatedAt
    );
  }
}
