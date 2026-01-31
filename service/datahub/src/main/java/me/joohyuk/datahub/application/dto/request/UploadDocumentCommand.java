package me.joohyuk.datahub.application.dto.request;

import com.spartaecommerce.domain.vo.UserId;
import me.joohyuk.datahub.domain.vo.CollectionId;

/**
 * 문서 업로드 커맨드
 * <p>
 * 파일 업로드 시 필요한 정보를 담는 DTO입니다. 실제 파일 내용은 InputStream으로 별도로 전달됩니다.
 */
public record UploadDocumentCommand(
    CollectionId collectionId,
    String fileName,         // 원본 파일명 (예: "document.md")
    Long fileSize,          // 파일 크기 (바이트)
    String contentType,     // MIME 타입 (예: "text/markdown")
    UserId uploadedBy         // 업로드한 사용자 ID
) {

  public UploadDocumentCommand {
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("File name cannot be empty");
    }
    if (fileSize == null || fileSize <= 0) {
      throw new IllegalArgumentException("File size must be positive");
    }
    if (contentType == null || contentType.isBlank()) {
      throw new IllegalArgumentException("Content type cannot be empty");
    }
  }
}
