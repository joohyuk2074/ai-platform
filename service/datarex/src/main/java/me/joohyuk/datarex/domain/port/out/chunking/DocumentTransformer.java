package me.joohyuk.datarex.domain.port.out.chunking;

import java.util.List;
import me.joohyuk.datarex.domain.vo.DocumentContent;

public interface DocumentTransformer {

  List<DocumentContent> transform(
      List<DocumentContent> documents,
      ChunkingConfig config
  );

  /**
   * 청킹 설정
   *
   * @param defaultChunkSize      기본 청크 크기 (토큰 수)
   * @param minChunkSizeChars     최소 청크 크기 (문자 수)
   * @param minChunkLengthToEmbed 임베딩할 최소 청크 길이
   * @param maxNumChunks          최대 청크 수
   * @param keepSeparator         구분자 유지 여부
   */
  record ChunkingConfig(
      int defaultChunkSize,
      int minChunkSizeChars,
      int minChunkLengthToEmbed,
      int maxNumChunks,
      boolean keepSeparator
  ) {

    public static ChunkingConfig defaultConfig() {
      return new ChunkingConfig(
          1000,  // defaultChunkSize
          400,   // minChunkSizeChars
          10,    // minChunkLengthToEmbed
          5000,  // maxNumChunks
          true   // keepSeparator
      );
    }
  }
}
