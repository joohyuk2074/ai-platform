package me.joohyuk.datahub.application.dto.request;

/**
 * 개별 청크 업로드 커맨드
 *
 * <p>세션 식별과 청크 위치 정보를 담습니다. 실제 청크 바이트 데이터는 InputStream으로 별도로 전달됩니다.
 */
public record UploadChunkCommand(
    String uploadId,   // initiateChunkedUpload에서 반환된 세션 ID
    int chunkIndex     // 현재 청크의 인덱스 (0-based)
) {

  public UploadChunkCommand {
    if (uploadId == null || uploadId.isBlank()) {
      throw new IllegalArgumentException("Upload ID cannot be empty");
    }
    if (chunkIndex < 0) {
      throw new IllegalArgumentException("Chunk index must be non-negative");
    }
  }
}