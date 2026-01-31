package me.joohyuk.datahub.infrastructure.adapter.out.persistence.entity;

import com.spartaecommerce.domain.vo.Metadata;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.vo.CollectionId;
import me.joohyuk.datahub.infrastructure.adapter.out.persistence.converter.MetadataConverter;

@Getter
@Entity
@Table(
    name = "documents",
    indexes = {
        @Index(name = "idx_documents_collection_id", columnList = "collection_id")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentJpaEntity {

  @Id
  @Column(name = "id")
  private Long id;

  @Column(name = "file_key", nullable = false)
  private String fileKey;

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
      Metadata metadata,
      Instant createdAt,
      Instant updatedAt
  ) {
    this.id = id;
    this.collectionId = collectionId;
    this.fileKey = fileKey;
    this.metadata = metadata;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static DocumentJpaEntity from(Document domain) {
    return new DocumentJpaEntity(
        domain.getId().getValue(),
        domain.getCollectionId().getValue(),
        domain.getFileKey(),
        domain.getMetadata(),
        domain.getCreatedAt(),
        domain.getUpdatedAt()
    );
  }

  public Document toDomain() {
    return Document.restore(
        this.id,
        new CollectionId(this.collectionId),
        this.fileKey,
        this.metadata,
        this.createdAt,
        this.updatedAt
    );
  }
}
