package me.joohyuk.datahub.domain.entity;

import com.spartaecommerce.domain.entity.AggregateRoot;
import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.domain.vo.Metadata;
import java.time.Instant;
import lombok.Getter;
import me.joohyuk.datahub.domain.vo.CollectionId;
import me.joohyuk.datahub.domain.vo.ContentHash;

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

  private Instant createdAt;
  private Instant updatedAt;

  private Document(CollectionId collectionId, String fileKey, ContentHash contentHash, Metadata metadata) {
    validate(collectionId, fileKey, contentHash, metadata);

    this.collectionId = collectionId;
    this.fileKey = fileKey;
    this.contentHash = contentHash;
    this.metadata = metadata;
  }

  public static Document create(
      CollectionId collectionId, String fileKey, ContentHash contentHash, Metadata metadata
  ) {
    return new Document(collectionId, fileKey, contentHash, metadata);
  }

  public static Document restore(
      DocumentId id,
      CollectionId collectionId,
      String fileKey,
      ContentHash contentHash,
      Metadata metadata,
      Instant createdAt,
      Instant updatedAt
  ) {
    validate(collectionId, fileKey, contentHash, metadata);

    Document doc = new Document(collectionId, fileKey, contentHash, metadata);
    doc.setId(id);          // AggregateRoot 보호 메서드/접근 가능해야 함
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

  private static void validate(
      CollectionId collectionId, String fileKey, ContentHash contentHash, Metadata metadata
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
  }
}
