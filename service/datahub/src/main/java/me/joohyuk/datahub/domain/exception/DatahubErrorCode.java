package me.joohyuk.datahub.domain.exception;

import com.spartaecommerce.exception.DomainErrorCode;

/**
 * Datahub 도메인 에러 코드
 *
 * <p>비즈니스 로직에서 발생하는 도메인 예외의 에러 코드를 정의합니다.
 * GlobalExceptionHandler에서 이 에러 코드를 기반으로 HTTP 상태 코드로 매핑됩니다.
 *
 * <p>네이밍 컨벤션:
 * <ul>
 *   <li>NOT_FOUND로 끝나는 경우 → HTTP 404</li>
 *   <li>ALREADY_EXISTS로 끝나는 경우 → HTTP 409</li>
 *   <li>UNAUTHORIZED로 끝나는 경우 → HTTP 401</li>
 *   <li>FORBIDDEN으로 끝나는 경우 → HTTP 403</li>
 *   <li>INVALID로 시작하는 경우 → HTTP 400</li>
 *   <li>그 외 → HTTP 422</li>
 * </ul>
 *
 * <p>사용 예시:
 * <pre>{@code
 * // 도메인 서비스나 애플리케이션 서비스에서 사용
 * throw new DomainException(
 *     "Document not found with id: " + documentId,
 *     DatahubDomainErrorCode.DOCUMENT_NOT_FOUND
 * );
 * }</pre>
 */
public enum DatahubErrorCode implements DomainErrorCode {

  // Document 관련 에러 (404)
  DOCUMENT_NOT_FOUND("DOCUMENT_NOT_FOUND"),
  DOCUMENT_COLLECTION_NOT_FOUND("DOCUMENT_COLLECTION_NOT_FOUND"),
  FILE_NOT_FOUND("FILE_NOT_FOUND"),

  // 중복 에러 (409)
  DOCUMENT_ALREADY_EXISTS("DOCUMENT_ALREADY_EXISTS"),
  COLLECTION_NAME_ALREADY_EXISTS("COLLECTION_NAME_ALREADY_EXISTS"),

  // 유효성 에러 (400)
  INVALID_REQUEST("INVALID_REQUEST"),
  INVALID_DOCUMENT_FORMAT("INVALID_DOCUMENT_FORMAT"),
  INVALID_FILE_TYPE("INVALID_FILE_TYPE"),
  INVALID_CHUNK_SIZE("INVALID_CHUNK_SIZE"),
  INVALID_FILE_NAME("INVALID_FILE_NAME"),
  INVALID_FILE_SIZE("INVALID_FILE_SIZE"),
  INVALID_CONTENT_TYPE("INVALID_CONTENT_TYPE"),
  INVALID_FILE_EMPTY("INVALID_FILE_EMPTY"),
  INVALID_COLLECTION_NAME("INVALID_COLLECTION_NAME"),
  INVALID_DOCUMENT_STATE("INVALID_DOCUMENT_STATE"),

  // 권한 에러 (403)
  COLLECTION_ACCESS_FORBIDDEN("COLLECTION_ACCESS_FORBIDDEN"),

  // 비즈니스 로직 에러 (422)
  DOCUMENT_PROCESSING_FAILED("DOCUMENT_PROCESSING_FAILED"),
  CHUNKING_FAILED("CHUNKING_FAILED"),
  EMBEDDING_FAILED("EMBEDDING_FAILED"),
  INDEXING_FAILED("INDEXING_FAILED"),
  UPLOAD_IN_PROGRESS("UPLOAD_IN_PROGRESS"),
  COLLECTION_NOT_EMPTY("COLLECTION_NOT_EMPTY"),
  FILE_STORAGE_FAILED("FILE_STORAGE_FAILED"),
  FILE_DELETE_FAILED("FILE_DELETE_FAILED"),
  ;

  private final String code;

  DatahubErrorCode(String code) {
    this.code = code;
  }

  @Override
  public String code() {
    return code;
  }
}
