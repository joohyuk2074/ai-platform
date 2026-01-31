package me.joohyuk.datahub.domain.entity;

import com.spartaecommerce.domain.entity.AggregateRoot;
import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.domain.vo.Metadata;
import java.time.Instant;
import lombok.Getter;
import me.joohyuk.datahub.domain.vo.CollectionId;

@Getter
public class Document extends AggregateRoot<DocumentId> {

  /**
   * 파일 저장소에서 원본 파일의 위치를 가리키는 키 예: "documents/1234567890_sample.md"
   */
  private final String fileKey;

  private final CollectionId collectionId;

  private final Metadata metadata;

  private Instant createdAt;
  private Instant updatedAt;

  private Document(CollectionId collectionId, String fileKey, Metadata metadata) {
    validate(collectionId, fileKey, metadata);

    this.collectionId = collectionId;
    this.fileKey = fileKey;
    this.metadata = metadata;
  }

  public static Document create(Long collectionId, String fileKey, Metadata metadata) {
    return new Document(new CollectionId(collectionId), fileKey, metadata);
  }

  public static Document restore(
      Long id,
      CollectionId collectionId,
      String fileKey,
      Metadata metadata,
      Instant createdAt,
      Instant updatedAt
  ) {
    validate(collectionId, fileKey, metadata);

    Document doc = new Document(collectionId, fileKey, metadata);
    doc.setId(new DocumentId(id));          // AggregateRoot 보호 메서드/접근 가능해야 함
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

  private static void validate(CollectionId collectionId, String fileKey, Metadata metadata) {
    if (collectionId == null) {
      throw new IllegalArgumentException("collectionId cannot be null");
    }

    if (fileKey == null || fileKey.isBlank()) {
      throw new IllegalArgumentException("File key cannot be empty");
    }

    if (metadata == null) {
      throw new IllegalArgumentException("Metadata cannot be null");
    }
  }
}
