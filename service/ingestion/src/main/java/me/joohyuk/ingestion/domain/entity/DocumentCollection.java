package me.joohyuk.ingestion.domain.entity;

import com.spartaecommerce.domain.entity.AggregateRoot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import me.joohyuk.ingestion.domain.exception.IngestionDomainException;
import me.joohyuk.ingestion.domain.vo.CollectionId;
import me.joohyuk.ingestion.domain.vo.DocumentId;
import me.joohyuk.ingestion.domain.vo.Metadata;

public class DocumentCollection extends AggregateRoot<CollectionId> {

  private final String name;
  private final String description;
  private final List<DocumentId> documentIds;
  private final Metadata metadata;
  private final Instant createdAt;
  private Instant updatedAt;

  private DocumentCollection(
      CollectionId id,
      String name,
      String description,
      List<DocumentId> documentIds,
      Metadata metadata,
      Instant createdAt,
      Instant updatedAt
  ) {
    super.setId(Objects.requireNonNull(id, "Collection ID cannot be null"));

    if (name == null || name.isBlank()) {
      throw new IngestionDomainException("Collection name cannot be null or empty");
    }
    this.name = name;
    this.description = description;
    this.documentIds = new ArrayList<>(documentIds);
    this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
    this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
    this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
  }

  /**
   * 새로운 컬렉션 생성
   */
  public static DocumentCollection create(
      String name,
      String description,
      Metadata metadata
  ) {
    Instant now = Instant.now();
    return new DocumentCollection(
        CollectionId.generate(),
        name,
        description,
        new ArrayList<>(),
        metadata,
        now,
        now
    );
  }

  /**
   * 기존 컬렉션 재구성
   */
  public static DocumentCollection reconstruct(
      CollectionId id,
      String name,
      String description,
      List<DocumentId> documentIds,
      Metadata metadata,
      Instant createdAt,
      Instant updatedAt
  ) {
    return new DocumentCollection(
        id,
        name,
        description,
        documentIds,
        metadata,
        createdAt,
        updatedAt
    );
  }

  /**
   * 문서 추가
   */
  public void addDocument(DocumentId documentId) {
    Objects.requireNonNull(documentId, "Document ID cannot be null");

    if (documentIds.contains(documentId)) {
      throw new IngestionDomainException(
          "Document already exists in collection: " + documentId
      );
    }

    documentIds.add(documentId);
    this.updatedAt = Instant.now();
  }

  /**
   * 문서 제거
   */
  public void removeDocument(DocumentId documentId) {
    Objects.requireNonNull(documentId, "Document ID cannot be null");

    boolean removed = documentIds.remove(documentId);
    if (!removed) {
      throw new IngestionDomainException(
          "Document not found in collection: " + documentId
      );
    }

    this.updatedAt = Instant.now();
  }

  /**
   * 문서가 컬렉션에 속하는지 확인
   */
  public boolean containsDocument(DocumentId documentId) {
    return documentIds.contains(documentId);
  }

  /**
   * 컬렉션이 비어있는지 확인
   */
  public boolean isEmpty() {
    return documentIds.isEmpty();
  }

  /**
   * 문서 수
   */
  public int getDocumentCount() {
    return documentIds.size();
  }

  // Getters
  public CollectionId getId() {
    return super.getId();
  }
}
