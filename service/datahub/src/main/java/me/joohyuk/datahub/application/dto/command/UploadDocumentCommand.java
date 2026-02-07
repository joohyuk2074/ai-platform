package me.joohyuk.datahub.application.dto.command;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.UserId;

public record UploadDocumentCommand(
    CollectionId collectionId,
    String fileName,         // 원본 파일명 (예: "message.md")
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
