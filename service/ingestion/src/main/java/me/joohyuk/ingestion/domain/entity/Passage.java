package me.joohyuk.ingestion.domain.entity;

import com.spartaecommerce.domain.entity.AggregateRoot;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import me.joohyuk.ingestion.domain.vo.ChunkId;
import me.joohyuk.ingestion.domain.vo.Content;
import me.joohyuk.ingestion.domain.vo.DocumentId;
import me.joohyuk.ingestion.domain.vo.Metadata;
import me.joohyuk.ingestion.domain.vo.PassageId;

@Getter
public class Passage extends AggregateRoot<PassageId> {

  private final ChunkId chunkId;
  private final DocumentId documentId;
  private final Content content;
  private final Metadata metadata;
  private final Instant createdAt;
  private boolean readyForEmbedding;

  private Passage(
      PassageId id,
      ChunkId chunkId,
      DocumentId documentId,
      Content content,
      Metadata metadata,
      Instant createdAt,
      boolean readyForEmbedding
  ) {
    super.setId(Objects.requireNonNull(id, "Passage ID cannot be null"));
    this.chunkId = Objects.requireNonNull(chunkId, "Chunk ID cannot be null");
    this.documentId = Objects.requireNonNull(documentId, "Document ID cannot be null");
    this.content = Objects.requireNonNull(content, "Content cannot be null");
    this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
    this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
    this.readyForEmbedding = readyForEmbedding;
  }

  /**
   * 청크로부터 Passage 생성
   *
   * @param chunk              원본 청크
   * @param enrichedContent    컨텍스트가 풍부해진 콘텐츠
   * @param additionalMetadata 추가 메타데이터
   */
  public static Passage fromChunk(
      Chunk chunk,
      Content enrichedContent,
      Metadata additionalMetadata
  ) {
    // 청크의 메타데이터와 추가 메타데이터 병합
    Metadata mergedMetadata = mergeMetadata(chunk.getMetadata(), additionalMetadata);

    return new Passage(
        PassageId.generate(),
        chunk.getId(),
        chunk.getDocumentId(),
        enrichedContent,
        mergedMetadata,
        Instant.now(),
        false
    );
  }

  /**
   * 기존 Passage 재구성
   */
  public static Passage reconstruct(
      PassageId id,
      ChunkId chunkId,
      DocumentId documentId,
      Content content,
      Metadata metadata,
      Instant createdAt,
      boolean readyForEmbedding
  ) {
    return new Passage(
        id,
        chunkId,
        documentId,
        content,
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
   * 메타데이터 병합 (청크 메타데이터 + 추가 메타데이터)
   */
  private static Metadata mergeMetadata(Metadata base, Metadata additional) {
    Metadata.Builder builder = Metadata.builder();

    // 기본 메타데이터 추가
    base.getAttributes().forEach(builder::put);

    // 추가 메타데이터 추가 (덮어쓰기)
    additional.getAttributes().forEach(builder::put);

    return builder.build();
  }

  public PassageId getId() {
    return super.getId();
  }
}
