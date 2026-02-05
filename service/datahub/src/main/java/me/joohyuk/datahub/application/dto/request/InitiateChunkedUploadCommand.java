package me.joohyuk.datahub.application.dto.request;

import com.spartaecommerce.domain.vo.CollectionId;

/**
 * 청크 업로드 시작 커맨드
 *
 * <p>멀티파트 청크 업로드를 초기화할 때 필요한 파일 메타데이터를 담습니다. 서비스는 이 정보를 기반으로
 * 업로드 세션과 청크 분할 계획을 생성합니다.
 */
public record InitiateChunkedUploadCommand(
    CollectionId collectionId,
    String fileName,      // 원본 파일명 (예: "message.md")
    long totalSize,       // 파일 전체 크기 (바이트)
    String contentType,   // MIME 타입 (예: "text/markdown")
    Long uploadedBy       // 업로드한 사용자 ID
) {

  public InitiateChunkedUploadCommand {
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("File name cannot be empty");
    }
    if (totalSize <= 0) {
      throw new IllegalArgumentException("Total size must be positive");
    }
    if (contentType == null || contentType.isBlank()) {
      throw new IllegalArgumentException("Content type cannot be empty");
    }
  }
}