package me.joohyuk.datahub.application.dto.command;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.UserId;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;

public record UploadDocumentCommand(
    CollectionId collectionId,
    String fileName,         // 원본 파일명 (예: "message.md")
    Long fileSize,          // 파일 크기 (바이트)
    String contentType,     // MIME 타입 (예: "text/markdown")
    UserId uploadedBy         // 업로드한 사용자 ID
) {

  public UploadDocumentCommand {
    if (fileName == null || fileName.isBlank()) {
      throw new DatahubDomainException(
          "File name cannot be empty",
          DatahubErrorCode.INVALID_FILE_NAME
      );
    }
    if (fileSize == null || fileSize <= 0) {
      throw new DatahubDomainException(
          "File size must be positive",
          DatahubErrorCode.INVALID_FILE_SIZE
      );
    }
    if (contentType == null || contentType.isBlank()) {
      throw new DatahubDomainException(
          "Content type cannot be empty",
          DatahubErrorCode.INVALID_CONTENT_TYPE
      );
    }
  }
}
