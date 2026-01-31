package me.joohyuk.datahub.domain.entity;

import com.spartaecommerce.domain.entity.AggregateRoot;
import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.domain.vo.Metadata;
import java.time.Instant;
import lombok.Getter;

@Getter
public class Document extends AggregateRoot<DocumentId> {

  /**
   * 파일 저장소에서 원본 파일의 위치를 가리키는 키 예: "documents/1234567890_sample.md"
   */
  private final String fileKey;

  private final Metadata metadata;

  private Instant createdAt;
  private Instant updatedAt;

  private Document(String fileKey, Metadata metadata) {
    if (fileKey == null || fileKey.isBlank()) {
      throw new IllegalArgumentException("File key cannot be empty");
    }
    if (metadata == null) {
      throw new IllegalArgumentException("Metadata cannot be null");
    }
    this.fileKey = fileKey;
    this.metadata = metadata;
  }

  public static Document create(String fileKey, Metadata metadata) {
    return new Document(fileKey, metadata);
  }

  public void initialize(Long id, Instant now) {
    super.setId(new DocumentId(id));
    this.createdAt = now;
    this.updatedAt = now;
  }

  public DocumentId getId() {
    return super.getId();
  }
}
