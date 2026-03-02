package me.joohyuk.datahub.infrastructure.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.joohyuk.datahub.domain.entity.DocumentCollection;

@Getter
@Entity
@Table(name = "document_collections")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentCollectionJpaEntity {

  @Id
  @Column(name = "id")
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public DocumentCollectionJpaEntity(
      Long id,
      String name,
      String description,
      Instant createdAt,
      Instant updatedAt
  ) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static DocumentCollectionJpaEntity from(DocumentCollection domain) {
    return new DocumentCollectionJpaEntity(
        domain.getId().getValue(),
        domain.getName(),
        domain.getDescription(),
        domain.getCreatedAt(),
        domain.getUpdatedAt()
    );
  }

  public DocumentCollection toDomain() {
    return DocumentCollection.of(
        id,
        name,
        description,
        createdAt,
        updatedAt
    );
  }
}
