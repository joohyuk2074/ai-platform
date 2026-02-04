package me.joohyuk.datarex.domain.port.out.chunking;

import java.util.List;
import me.joohyuk.datarex.domain.model.DocumentContent;

/**
 * Spring AI의 DocumentTransformer 패턴을 따르는 문서 변환 인터페이스
 * 문서를 청킹(chunking)하여 더 작은 단위로 분할합니다.
 */
public interface DocumentTransformer {

  /**
   * 문서 리스트를 청킹하여 변환합니다.
   *
   * @param documents 변환할 문서 리스트
   * @param config 청킹 설정
   * @return 청킹된 문서 리스트
   */
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

    /**
     * 기본 청킹 설정을 생성
     */
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
