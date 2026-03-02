package me.joohyuk.vecdash.domain.entity;

import com.spartaecommerce.domain.entity.AggregateRoot;
import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.domain.vo.Metadata;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import me.joohyuk.vecdash.domain.vo.Content;
import me.joohyuk.vecdash.domain.vo.PassageId;

@Getter
public class Passage extends AggregateRoot<PassageId> {

  private final DocumentId documentId;
  private final Content content;
  private final int sequence;
  private final int startPosition;
  private final int endPosition;
  private final Metadata metadata;
  private final Instant createdAt;
  private boolean readyForEmbedding;

  private Passage(
      PassageId id,
      DocumentId documentId,
      Content content,
      int sequence,
      int startPosition,
      int endPosition,
      Metadata metadata,
      Instant createdAt,
      boolean readyForEmbedding
  ) {
    super.setId(Objects.requireNonNull(id, "Passage ID cannot be null"));
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
    this.readyForEmbedding = readyForEmbedding;
  }

  /**
   * 청킹 서비스로부터 받은 데이터로 Passage 생성
   *
   * @param chunkId       청크 ID (추적용)
   * @param documentId    문서 ID
   * @param content       청킹된 콘텐츠
   * @param sequence      청크 순서
   * @param startPosition 원본 문서에서의 시작 위치
   * @param endPosition   원본 문서에서의 종료 위치
   * @param metadata      메타데이터
   */
  public static Passage create(
      DocumentId documentId,
      Content content,
      int sequence,
      int startPosition,
      int endPosition,
      Metadata metadata
  ) {
    return new Passage(
        PassageId.generate(),
        documentId,
        content,
        sequence,
        startPosition,
        endPosition,
        metadata,
        Instant.now(),
        false
    );
  }

  /**
   * 기존 Passage 재구성 (DB에서 로드 시 사용)
   */
  public static Passage reconstruct(
      PassageId id,
      DocumentId documentId,
      Content content,
      int sequence,
      int startPosition,
      int endPosition,
      Metadata metadata,
      Instant createdAt,
      boolean readyForEmbedding
  ) {
    return new Passage(
        id,
        documentId,
        content,
        sequence,
        startPosition,
        endPosition,
        metadata,
        createdAt,
        readyForEmbedding
    );
  }

  /**
   * 임베딩 준비 완료 표시
   */
  public void markReadyForEmbedding() {
    if (content.isEmpty()) {
      throw new IllegalStateException("Cannot mark passage with empty content as ready");
    }
    this.readyForEmbedding = true;
  }

  /**
   * Passage가 임베딩 가능한 상태인지 검증
   */
  public boolean canBeEmbedded() {
    return readyForEmbedding && !content.isEmpty();
  }

  /**
   * 다음 Passage와의 중복 영역 계산
   */
  public boolean overlapsWith(Passage other) {
    if (!this.documentId.equals(other.documentId)) {
      return false;
    }
    return this.endPosition > other.startPosition && this.startPosition < other.endPosition;
  }

  public PassageId getId() {
    return super.getId();
  }
}
