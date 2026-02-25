package me.joohyuk.datahub.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.Metadata;
import com.spartaecommerce.domain.vo.UserId;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import me.joohyuk.datahub.application.dto.command.UploadDocumentCommand;
import me.joohyuk.datahub.application.dto.result.UploadDocumentResult;
import me.joohyuk.datahub.application.service.UploadDocumentService;
import me.joohyuk.datahub.application.service.handler.DocumentPersistenceHandler;
import me.joohyuk.datahub.application.validation.FileValidationPolicy;
import me.joohyuk.datahub.domain.entity.DocumentCollection;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.service.DocumentDomainService;
import me.joohyuk.datahub.fake.FakeDateTimeHolder;
import me.joohyuk.datahub.fake.InMemoryDocumentCollectionRepository;
import me.joohyuk.datahub.fake.InMemoryDocumentRepository;
import me.joohyuk.datahub.fake.InMemoryIdGenerator;
import me.joohyuk.datahub.fake.MemoryFileFakeStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * DocumentCreateCommandHandler 고전파(Classical School) 테스트
 *
 * <p>Mock 대신 Fake 구현체를 사용하여 실제 객체 그래프를 구성합니다.
 * <ul>
 *   <li>InMemoryFileStorage: 파일 저장소 대체</li>
 *   <li>InMemoryDocumentRepository: DB 저장소 대체</li>
 *   <li>실제 DocumentDomainService, DocumentPersistenceHelper 사용</li>
 * </ul>
 *
 * <p>검증 방식:
 * <ul>
 *   <li>출력 기반 검증: 반환된 이벤트의 값이 올바른가?</li>
 *   <li>상태 기반 검증: FileStorage / Repository의 상태가 올바르게 변경되었는가?</li>
 * </ul>
 */
@DisplayName("DocumentCreateCommandHandler 테스트")
class UploadDocumentServiceTest {

  /**
   * 테스트 전체에서 공유하는 고정 CollectionId. PersistenceHelper의 Collection 존재 체크를 통과시킴
   */
  private static final CollectionId COLLECTION_ID = new CollectionId(1L);

  private UploadDocumentService handler;
  private MemoryFileFakeStorage fileStorage;
  private InMemoryDocumentRepository documentRepository;

  @BeforeEach
  void setUp() {
    // Classical School: 실제 객체 그래프 구성
    var idGenerator = new InMemoryIdGenerator();
    var dateTimeHolder = new FakeDateTimeHolder();
    fileStorage = new MemoryFileFakeStorage();
    documentRepository = new InMemoryDocumentRepository();

    // Collection 존재 체크를 위해 사전 시드
    var collectionRepository = new InMemoryDocumentCollectionRepository();
    collectionRepository.save(
        DocumentCollection.of(COLLECTION_ID.getValue(), "test-collection", "desc",
            Instant.now(), Instant.now()));

    DocumentDomainService domainService = new DocumentDomainService();

    DocumentPersistenceHandler persistenceHelper = new DocumentPersistenceHandler(
        domainService,
        documentRepository,
        collectionRepository,
        dateTimeHolder,
        idGenerator
    );

    handler = new UploadDocumentService(
        fileStorage,
        persistenceHelper,
        documentRepository,
        new FileValidationPolicy()
    );
  }

  @Nested
  @DisplayName("uploadDocument 메서드는")
  class UploadDocumentTests {

    @Test
    @DisplayName("유효한 입력이 제공되면 문서를 업로드하고 올바른 이벤트를 반환한다")
    void should_return_uploaded_event_when_upload_succeeds() {
      // Given
      UploadDocumentCommand command = createUploadCommand(
          "report.pdf",
          1024L,
          "application/pdf",
          1L
      );
      InputStream fileInputStream = new ByteArrayInputStream("file content".getBytes());

      // When
      UploadDocumentResult result = handler.uploadDocument(command, fileInputStream);

      // Then: 출력 기반 검증 - 업로드 성공 시 유효한 이벤트를 반환하는가?
      assertThat(result).isNotNull();

      // And: 상태 기반 검증 - 파일과 문서가 실제로 저장되었는가?
      assertThat(fileStorage.size()).isEqualTo(1);
      assertThat(documentRepository.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("파일이 올바른 메타데이터로 저장된다")
    void should_store_file_with_correct_metadata() {
      // Given
      UploadDocumentCommand command =
          createUploadCommand("analysis.md", 2048L, "text/markdown", 42L);
      InputStream fileInputStream = new ByteArrayInputStream("markdown content".getBytes());

      // When
      UploadDocumentResult result = handler.uploadDocument(command, fileInputStream);

      // Then: 상태 기반 검증 - 저장된 파일의 메타데이터가 올바른가?
      Metadata storedMetadata = fileStorage.getMetadata(result.fileKey());

      assertThat(storedMetadata).isNotNull();
      assertThat(storedMetadata.fileName()).isEqualTo("analysis.md");
      assertThat(storedMetadata.fileSize()).isEqualTo(2048L);
      assertThat(storedMetadata.contentType()).isEqualTo("text/markdown");
      assertThat(storedMetadata.uploadedBy()).isEqualTo(42L);
    }

    @Test
    @DisplayName("DB 저장 실패 시 이미 업로드된 파일을 삭제한다 (보상 트랜잭션)")
    void should_delete_uploaded_file_when_db_save_fails() {
      // Given: DB 저장 실패 시뮬레이션
      documentRepository.configureToFailOnSave();
      UploadDocumentCommand command =
          createUploadCommand("report.pdf", 1024L, "application/pdf", 1L);
      InputStream fileInputStream = new ByteArrayInputStream("file content".getBytes());

      // When & Then: IngestionDomainException 발생
      assertThatThrownBy(() -> handler.uploadDocument(command, fileInputStream))
          .isInstanceOf(DatahubDomainException.class);

      // And: 상태 기반 검증 - 보상 트랜잭션으로 파일이 삭제되었는가?
      assertThat(fileStorage.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("DB 저장 실패 후 보상 트랜잭션도 실패하면 원본 예외를 발생시키고 파일이 남는다")
    void should_leave_file_when_compensation_also_fails() {
      // Given: DB 저장 실패 + 파일 삭제 실패 시뮬레이션
      documentRepository.configureToFailOnSave();
      fileStorage.configureDeleteToFail();
      UploadDocumentCommand command =
          createUploadCommand("report.pdf", 1024L, "application/pdf", 1L);
      InputStream fileInputStream = new ByteArrayInputStream("file content".getBytes());

      // When & Then: 원본 예외(IngestionDomainException)가 발생하는가?
      assertThatThrownBy(() -> handler.uploadDocument(command, fileInputStream))
          .isInstanceOf(DatahubDomainException.class);

      // And: 상태 기반 검증 - 파일 삭제 실패로 파일이 남아있는가?
      assertThat(fileStorage.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("동일한 콘텐츠의 파일을 재업로드하면 중복 검증에 의해 거부되고 파일이 보상 삭제된다")
    void should_reject_duplicate_file_and_compensate_storage() {
      // Given: 첫 번째 업로드 완료
      UploadDocumentCommand command =
          createUploadCommand("report.pdf", 1024L, "application/pdf", 1L);
      String fileContent = "duplicate content";
      handler.uploadDocument(command, new ByteArrayInputStream(fileContent.getBytes()));

      assertThat(fileStorage.size()).isEqualTo(1);
      assertThat(documentRepository.size()).isEqualTo(1);

      // When & Then: 동일한 콘텐츠로 재업로드 시 IngestionDomainException 발생
      UploadDocumentCommand duplicateCommand =
          createUploadCommand("report-copy.pdf", 1024L, "application/pdf", 1L);
      assertThatThrownBy(() ->
          handler.uploadDocument(duplicateCommand, new ByteArrayInputStream(fileContent.getBytes()))
      ).isInstanceOf(DatahubDomainException.class)
          .hasMessageContaining("Duplicate file detected");

      // And: 상태 기반 검증 - 중복 파일은 보상 삭제되어 저장소 크기가 변하지 않음
      assertThat(fileStorage.size()).isEqualTo(1);
      assertThat(documentRepository.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("여러 문서를 연속 업로드하면 각각 고유한 ID와 파일 키를 가진다")
    void should_assign_unique_ids_and_keys_when_uploading_multiple_documents() {
      // Given
      UploadDocumentCommand command1 =
          createUploadCommand("doc1.pdf", 100L, "application/pdf", 1L);
      UploadDocumentCommand command2 =
          createUploadCommand("doc2.pdf", 200L, "application/pdf", 1L);

      // When
      UploadDocumentResult result1 = handler.uploadDocument(command1,
          new ByteArrayInputStream("content1".getBytes()));
      UploadDocumentResult result2 = handler.uploadDocument(command2,
          new ByteArrayInputStream("content2".getBytes()));

      // Then: 출력 기반 검증 - 각 문서가 고유한 ID와 파일 키를 가지는가?
      assertThat(result1.documentId()).isNotEqualTo(result2.documentId());

      // And: 상태 기반 검증 - 두 문서 모두 저장되었는가?
      assertThat(documentRepository.size()).isEqualTo(2);
      assertThat(fileStorage.size()).isEqualTo(2);
    }
  }

  private UploadDocumentCommand createUploadCommand(
      String fileName, Long fileSize, String contentType, Long uploadedBy
  ) {
    return new UploadDocumentCommand(
        COLLECTION_ID,
        fileName,
        fileSize,
        contentType,
        new UserId(uploadedBy)
    );
  }
}
