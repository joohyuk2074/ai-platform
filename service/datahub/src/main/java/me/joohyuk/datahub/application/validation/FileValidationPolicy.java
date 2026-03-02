package me.joohyuk.datahub.application.validation;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.command.UploadDocumentCommand;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import org.springframework.stereotype.Component;

/**
 * 파일 업로드 검증 정책
 *
 * <p>파일 업로드 시 적용되는 비즈니스 규칙과 정책을 정의합니다.
 * Application Service 계층의 검증 책임을 분리하여 단일 책임 원칙을 준수합니다.
 *
 * <p>검증 항목:
 * <ul>
 *   <li>파일 크기 제한 (최대 100MB)</li>
 *   <li>파일 형식 검증 (허용된 ContentType)</li>
 *   <li>파일 이름 유효성 (길이, 위험한 문자)</li>
 * </ul>
 */
@Slf4j
@Component
public class FileValidationPolicy {

  private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
  private static final int MAX_FILENAME_LENGTH = 255;

  private static final Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
      "application/pdf",
      "application/msword",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "application/vnd.ms-excel",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "text/plain",
      "text/csv",
      "text/markdown"
  );

  /**
   * 파일 업로드 전 종합 검증 수행
   *
   * @param command 업로드 커맨드
   * @throws DatahubDomainException 검증 실패 시
   */
  public void validate(UploadDocumentCommand command) {
    validateFileSize(command.fileSize());
    validateFileFormat(command.contentType(), command.fileName());
    validateFileName(command.fileName());
  }

  /**
   * 파일 크기 검증 최대 100MB까지 허용
   *
   * <p>Note: 기본 검증(fileSize > 0)은 UploadDocumentCommand 생성자에서 이미 수행됨
   */
  private void validateFileSize(long fileSize) {
    if (fileSize > MAX_FILE_SIZE) {
      throw new DatahubDomainException(
          String.format("File size exceeds maximum allowed size of %d bytes", MAX_FILE_SIZE),
          DatahubErrorCode.INVALID_FILE_SIZE
      );
    }

    log.debug("File size validation passed: {} bytes", fileSize);
  }

  /**
   * 파일 형식 검증
   * - 허용된 ContentType 확인
   * - 파일 확장자와 ContentType 일치 확인
   *
   * <p>Note: 기본 검증(contentType not null/blank)은 UploadDocumentCommand 생성자에서 이미 수행됨
   */
  private void validateFileFormat(String contentType, String fileName) {
    if (!ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
      throw new DatahubDomainException(
          String.format("Unsupported file type: %s", contentType),
          DatahubErrorCode.INVALID_FILE_TYPE
      );
    }

    // 확장자 검증
    String extension = getFileExtension(fileName);
    if (!isExtensionMatchingContentType(extension, contentType)) {
      log.warn("File extension '{}' does not match content type '{}'", extension, contentType);
      // 경고만 로그하고 진행 (일부 시스템에서 contentType이 정확하지 않을 수 있음)
    }

    log.debug("File format validation passed: contentType={}, fileName={}",
        contentType, fileName);
  }

  /**
   * 파일 이름 검증
   * - 최대 길이 제한
   * - 위험한 문자 제거
   *
   * <p>Note: 기본 검증(fileName not null/blank)은 UploadDocumentCommand 생성자에서 이미 수행됨
   */
  private void validateFileName(String fileName) {
    if (fileName.length() > MAX_FILENAME_LENGTH) {
      throw new DatahubDomainException(
          String.format("File name exceeds maximum length of %d characters", MAX_FILENAME_LENGTH),
          DatahubErrorCode.INVALID_FILE_NAME
      );
    }

    // 위험한 경로 순회 패턴 검증
    if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
      throw new DatahubDomainException(
          "File name contains invalid characters",
          DatahubErrorCode.INVALID_FILE_NAME
      );
    }

    log.debug("File name validation passed: {}", fileName);
  }

  private String getFileExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
      return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
    return "";
  }

  private boolean isExtensionMatchingContentType(String extension, String contentType) {
    return switch (extension) {
      case "pdf" -> contentType.equals("application/pdf");
      case "doc" -> contentType.equals("application/msword");
      case "docx" -> contentType.equals(
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
      case "xls" -> contentType.equals("application/vnd.ms-excel");
      case "xlsx" -> contentType.equals(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      case "txt" -> contentType.equals("text/plain");
      case "csv" -> contentType.equals("text/csv");
      case "md" -> contentType.equals("text/markdown");
      default -> true; // 알 수 없는 확장자는 경고만 하고 통과
    };
  }
}
