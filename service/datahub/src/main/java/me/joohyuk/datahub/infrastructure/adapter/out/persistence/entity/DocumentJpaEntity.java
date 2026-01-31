package me.joohyuk.datahub.infrastructure.adapter.out.persistence.entity;

import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.domain.vo.Metadata;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.vo.CollectionId;
import me.joohyuk.datahub.domain.vo.ContentHash;
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
      Instant createdAt,
      Instant updatedAt
  ) {
    this.id = id;
    this.collectionId = collectionId;
    this.fileKey = fileKey;
    this.contentHash = contentHash;
    this.metadata = metadata;
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
        this.createdAt,
        this.updatedAt
    );
  }
}
