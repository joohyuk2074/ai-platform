package me.joohyuk.ingestion.domain.entity;

import com.spartaecommerce.domain.entity.BaseEntity;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import me.joohyuk.ingestion.domain.vo.ChunkId;
import me.joohyuk.ingestion.domain.vo.Content;
import me.joohyuk.ingestion.domain.vo.DocumentId;
import me.joohyuk.ingestion.domain.vo.Metadata;

@Getter
public class Chunk extends BaseEntity<ChunkId> {

  private final DocumentId documentId;
  private final Content content;
  private final int sequence;
  private final int startPosition;
  private final int endPosition;
  private final Metadata metadata;
  private final Instant createdAt;

  private Chunk(
      ChunkId id,
      DocumentId documentId,
      Content content,
      int sequence,
      int startPosition,
      int endPosition,
      Metadata metadata,
      Instant createdAt
  ) {
    super.setId(Objects.requireNonNull(id, "Chunk ID cannot be null"));
    this.documentId = Objects.requireNonNull(documentId, "Document ID cannot be null");
    this.content = Objects.requireNonNull(content, "Content cannot be null");

    if (sequence < 0) {
      throw new IllegalArgumentException("Sequence must be non-negative");
    }
    if (startPosition < 0) {
      throw new IllegalArgumentException("Start position must be non-negative");
    }
    if (endPosition <= startPosition) {
      throw new IllegalArgumentException("End position must be greater than start position");
    }

    this.sequence = sequence;
    this.startPosition = startPosition;
    this.endPosition = endPosition;
    this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
    this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
  }

  public static Chunk create(
      DocumentId documentId,
      Content content,
      int sequence,
      int startPosition,
      int endPosition,
      Metadata metadata
  ) {
    return new Chunk(
        ChunkId.generate(),
        documentId,
        content,
        sequence,
        startPosition,
        endPosition,
        metadata,
        Instant.now()
    );
  }

  public static Chunk reconstruct(
      ChunkId id,
      DocumentId documentId,
      Content content,
      int sequence,
      int startPosition,
      int endPosition,
      Metadata metadata,
      Instant createdAt
  ) {
    return new Chunk(
        id,
        documentId,
        content,
        sequence,
        startPosition,
        endPosition,
        metadata,
        createdAt
    );
  }

  public boolean isValidSize(int minSize, int maxSize) {
    int length = content.getLength();
    return length >= minSize && length <= maxSize;
  }

  /**
   * 다음 청크와의 중복 영역 계산
   */
  public boolean overlapsWith(Chunk other) {
    if (!this.documentId.equals(other.documentId)) {
      return false;
    }
    return this.endPosition > other.startPosition && this.startPosition < other.endPosition;
  }

  public ChunkId getId() {
    return super.getId();
  }
}
