package me.joohyuk.datahub.application.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.UserId;
import java.util.stream.Stream;
import me.joohyuk.datahub.application.dto.command.UploadDocumentCommand;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * FileValidationPolicy 테스트
 *
 * <p>Classical/Detroit School 원칙 적용:
 * <ul>
 *   <li>실제 객체만 사용 (mocking 없음)</li>
 *   <li>Public API(validate 메서드)를 통한 블랙박스 테스트</li>
 *   <li>출력 기반 테스트: 예외 발생 여부와 에러 코드로 동작 검증</li>
 *   <li>구현 세부사항(로깅, 내부 메서드)에 결합되지 않음</li>
 * </ul>
 */
@DisplayName("FileValidationPolicy 파일 검증 정책 테스트")
class FileValidationPolicyTest {

  private final FileValidationPolicy fileValidationPolicy = new FileValidationPolicy();

  @Nested
  @DisplayName("파일 크기 검증")
  class FileSizeValidation {

    @Test
    @DisplayName("should_throw_INVALID_FILE_SIZE_when_file_size_exceeds_100MB")
    void should_throw_INVALID_FILE_SIZE_when_file_size_exceeds_100MB() {
      // Given: 100MB를 초과하는 파일 (100MB + 1 byte)
      long maxFileSizeExceeded = 100 * 1024 * 1024 + 1;
      var command = createCommand(
          "large-document.pdf",
          maxFileSizeExceeded,
          "application/pdf"
      );

      // When & Then: 검증 시 INVALID_FILE_SIZE 예외 발생
      assertThatThrownBy(() -> fileValidationPolicy.validate(command))
          .isInstanceOf(DatahubDomainException.class)
          .hasMessageContaining("File size exceeds maximum allowed size")
          .extracting("errorCode")
          .isEqualTo(DatahubErrorCode.INVALID_FILE_SIZE);
    }

    @Test
    @DisplayName("should_pass_validation_when_file_size_is_exactly_100MB")
    void should_pass_validation_when_file_size_is_exactly_100MB() {
      // Given: 정확히 100MB 크기의 파일
      long exactlyMaxFileSize = 100 * 1024 * 1024;
      var command = createCommand(
          "max-size-document.pdf",
          exactlyMaxFileSize,
          "application/pdf"
      );

      // When & Then: 검증 통과
      assertThatCode(() -> fileValidationPolicy.validate(command))
          .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should_pass_validation_when_file_size_is_within_valid_range")
    void should_pass_validation_when_file_size_is_within_valid_range() {
      // Given: 유효한 범위 내의 파일 크기 (1MB)
      long validFileSize = 1024 * 1024;
      var command = createCommand(
          "normal-document.pdf",
          validFileSize,
          "application/pdf"
      );

      // When & Then: 검증 통과
      assertThatCode(() -> fileValidationPolicy.validate(command))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("파일 형식 검증")
  class FileFormatValidation {

    @ParameterizedTest
    @ValueSource(strings = {
        "application/json",
        "image/png",
        "video/mp4",
        "audio/mpeg",
        "application/zip",
        "text/html"
    })
    @DisplayName("should_throw_INVALID_FILE_TYPE_when_content_type_is_not_allowed")
    void should_throw_INVALID_FILE_TYPE_when_content_type_is_not_allowed(
        String unsupportedContentType) {
      // Given: 허용되지 않은 contentType
      var command = createCommand(
          "file.ext",
          1024L,
          unsupportedContentType
      );

      // When & Then: 검증 시 INVALID_FILE_TYPE 예외 발생
      assertThatThrownBy(() -> fileValidationPolicy.validate(command))
          .isInstanceOf(DatahubDomainException.class)
          .hasMessageContaining("Unsupported file type")
          .extracting("errorCode")
          .isEqualTo(DatahubErrorCode.INVALID_FILE_TYPE);
    }

    @ParameterizedTest
    @MethodSource("provideAllowedContentTypes")
    @DisplayName("should_pass_validation_when_content_type_is_allowed")
    void should_pass_validation_when_content_type_is_allowed(
        String contentType,
        String fileName,
        String description) {
      // Given: 허용된 contentType과 적절한 파일명
      var command = createCommand(fileName, 1024L, contentType);

      // When & Then: 검증 통과
      assertThatCode(() -> fileValidationPolicy.validate(command))
          .doesNotThrowAnyException();
    }

    static Stream<Arguments> provideAllowedContentTypes() {
      return Stream.of(
          Arguments.of("application/pdf", "document.pdf", "PDF 문서"),
          Arguments.of("application/msword", "document.doc", "Word 97-2003 문서"),
          Arguments.of(
              "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
              "document.docx",
              "Word 문서"
          ),
          Arguments.of("application/vnd.ms-excel", "spreadsheet.xls", "Excel 97-2003 문서"),
          Arguments.of(
              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
              "spreadsheet.xlsx",
              "Excel 문서"
          ),
          Arguments.of("text/plain", "readme.txt", "텍스트 파일"),
          Arguments.of("text/csv", "data.csv", "CSV 파일"),
          Arguments.of("text/markdown", "readme.md", "Markdown 파일")
      );
    }

    @Test
    @DisplayName("should_pass_validation_when_content_type_is_lowercase")
    void should_pass_validation_when_content_type_is_lowercase() {
      // Given: 소문자로 된 contentType
      var command = createCommand(
          "document.pdf",
          1024L,
          "application/pdf"
      );

      // When & Then: 검증 통과
      assertThatCode(() -> fileValidationPolicy.validate(command))
          .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should_pass_validation_when_content_type_has_mixed_case")
    void should_pass_validation_when_content_type_has_mixed_case() {
      // Given: 대소문자가 섞인 contentType (내부적으로 toLowerCase 처리됨)
      var command = createCommand(
          "document.pdf",
          1024L,
          "APPLICATION/PDF"
      );

      // When & Then: 검증 통과
      assertThatCode(() -> fileValidationPolicy.validate(command))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("파일명 검증")
  class FileNameValidation {

    @Test
    @DisplayName("should_throw_INVALID_FILE_NAME_when_file_name_exceeds_255_characters")
    void should_throw_INVALID_FILE_NAME_when_file_name_exceeds_255_characters() {
      // Given: 255자를 초과하는 파일명 (256자)
      String tooLongFileName = "a".repeat(252) + ".pdf"; // 252 + 4 = 256
      var command = createCommand(
          tooLongFileName,
          1024L,
          "application/pdf"
      );

      // When & Then: 검증 시 INVALID_FILE_NAME 예외 발생
      assertThatThrownBy(() -> fileValidationPolicy.validate(command))
          .isInstanceOf(DatahubDomainException.class)
          .hasMessageContaining("File name exceeds maximum length of 255 characters")
          .extracting("errorCode")
          .isEqualTo(DatahubErrorCode.INVALID_FILE_NAME);
    }

    @Test
    @DisplayName("should_pass_validation_when_file_name_is_exactly_255_characters")
    void should_pass_validation_when_file_name_is_exactly_255_characters() {
      // Given: 정확히 255자인 파일명
      String maxLengthFileName = "a".repeat(251) + ".pdf"; // 251 + 4 = 255
      var command = createCommand(
          maxLengthFileName,
          1024L,
          "application/pdf"
      );

      // When & Then: 검증 통과
      assertThatCode(() -> fileValidationPolicy.validate(command))
          .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("provideDangerousFileNames")
    @DisplayName("should_throw_INVALID_FILE_NAME_when_file_name_contains_dangerous_characters")
    void should_throw_INVALID_FILE_NAME_when_file_name_contains_dangerous_characters(
        String dangerousFileName,
        String reason) {
      // Given: 위험한 문자를 포함한 파일명
      var command = createCommand(
          dangerousFileName,
          1024L,
          "application/pdf"
      );

      // When & Then: 검증 시 INVALID_FILE_NAME 예외 발생
      assertThatThrownBy(() -> fileValidationPolicy.validate(command))
          .isInstanceOf(DatahubDomainException.class)
          .hasMessageContaining("File name contains invalid characters")
          .extracting("errorCode")
          .isEqualTo(DatahubErrorCode.INVALID_FILE_NAME);
    }

    static Stream<Arguments> provideDangerousFileNames() {
      return Stream.of(
          Arguments.of("../etc/passwd", "경로 순회 공격 (..)"),
          Arguments.of("../../secret.txt", "다중 경로 순회"),
          Arguments.of("folder/../file.pdf", "중간 경로 순회"),
          Arguments.of("/etc/passwd", "절대 경로 (/)"),
          Arguments.of("folder/file.pdf", "하위 경로 (/)"),
          Arguments.of("C:\\Windows\\system32\\file.txt", "Windows 절대 경로 (\\)"),
          Arguments.of("folder\\file.pdf", "Windows 경로 구분자 (\\)")
      );
    }

    @Test
    @DisplayName("should_pass_validation_when_file_name_is_valid")
    void should_pass_validation_when_file_name_is_valid() {
      // Given: 유효한 파일명들
      var validFileNames = new String[]{
          "document.pdf",
          "my-file-2024.docx",
          "report_final.xlsx",
          "readme.txt",
          "파일명한글.pdf",
          "file (1).pdf",
          "data-2024-01-01.csv"
      };

      for (String validFileName : validFileNames) {
        var command = createCommand(
            validFileName,
            1024L,
            "application/pdf"
        );

        // When & Then: 모든 유효한 파일명에 대해 검증 통과
        assertThatCode(() -> fileValidationPolicy.validate(command))
            .as("File name '%s' should be valid", validFileName)
            .doesNotThrowAnyException();
      }
    }
  }

  @Nested
  @DisplayName("통합 시나리오 검증")
  class IntegrationScenarios {

    @Test
    @DisplayName("should_pass_all_validations_when_all_conditions_are_satisfied")
    void should_pass_all_validations_when_all_conditions_are_satisfied() {
      // Given: 모든 검증 조건을 만족하는 완벽한 커맨드
      var command = createCommand(
          "business-report-2024.pdf",
          (long) (5 * 1024 * 1024), // 5MB
          "application/pdf"
      );

      // When & Then: 모든 검증 통과
      assertThatCode(() -> fileValidationPolicy.validate(command))
          .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should_fail_fast_on_first_validation_error_when_multiple_errors_exist")
    void should_fail_fast_on_first_validation_error_when_multiple_errors_exist() {
      // Given: 파일 크기와 파일명 모두 잘못된 커맨드
      long maxFileSizeExceeded = 100 * 1024 * 1024 + 1; // 100MB 초과
      var command = createCommand(
          "../../../etc/passwd", // 잘못된 파일명
          maxFileSizeExceeded,   // 잘못된 파일 크기
          "application/pdf"
      );

      // When & Then: 첫 번째 검증(파일 크기)에서 실패 (fail-fast 동작)
      assertThatThrownBy(() -> fileValidationPolicy.validate(command))
          .isInstanceOf(DatahubDomainException.class)
          .extracting("errorCode")
          .isEqualTo(DatahubErrorCode.INVALID_FILE_SIZE); // 크기 검증이 먼저 실행됨
    }

    @Test
    @DisplayName("should_validate_all_fields_independently_when_called_multiple_times")
    void should_validate_all_fields_independently_when_called_multiple_times() {
      // Given: 여러 개의 서로 다른 유효한 커맨드
      var commands = new UploadDocumentCommand[]{
          createCommand("document1.pdf", 1024L, "application/pdf"),
          createCommand("document2.docx", 2048L,
              "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
          createCommand("data.csv", 512L, "text/csv")
      };

      // When & Then: 각 커맨드가 독립적으로 검증되어 모두 통과
      for (var command : commands) {
        assertThatCode(() -> fileValidationPolicy.validate(command))
            .doesNotThrowAnyException();
      }
    }

    @Test
    @DisplayName("should_validate_boundary_conditions_for_real_world_scenario")
    void should_validate_boundary_conditions_for_real_world_scenario() {
      // Given: 경계값에 가까운 실제 시나리오
      // - 파일 크기: 99.9MB (경계 근처)
      // - 파일명: 200자 (허용 범위 내)
      // - contentType: 대소문자 섞임
      long nearMaxFileSize = (long) (99.9 * 1024 * 1024);
      String longButValidFileName = "a".repeat(196) + ".pdf"; // 196 + 4 = 200
      var command = createCommand(
          longButValidFileName,
          nearMaxFileSize,
          "Application/PDF" // 대소문자 섞임
      );

      // When & Then: 실제 시나리오에서도 정상 동작
      assertThatCode(() -> fileValidationPolicy.validate(command))
          .doesNotThrowAnyException();
    }
  }

  /**
   * 테스트용 UploadDocumentCommand 생성 헬퍼 메서드
   *
   * <p>Given 단계를 간결하게 만들고, 테스트 데이터 생성 의도를 명확히 표현하기 위한 팩토리 메서드입니다.
   * 테스트에 필요하지 않은 필드(collectionId, uploadedBy)는 기본값으로 설정합니다.
   *
   * @param fileName    파일명
   * @param fileSize    파일 크기
   * @param contentType 콘텐츠 타입
   * @return 테스트용 UploadDocumentCommand 인스턴스
   */
  private UploadDocumentCommand createCommand(
      String fileName,
      Long fileSize,
      String contentType
  ) {
    return new UploadDocumentCommand(
        CollectionId.of(1L),        // 테스트에서 중요하지 않은 필드
        fileName,
        fileSize,
        contentType,
        UserId.of(100L)             // 테스트에서 중요하지 않은 필드
    );
  }
}
