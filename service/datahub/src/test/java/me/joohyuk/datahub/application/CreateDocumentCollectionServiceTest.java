package me.joohyuk.datahub.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spartaecommerce.domain.vo.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import me.joohyuk.datahub.application.dto.command.CreateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.result.CreateDocumentCollectionResult;
import me.joohyuk.datahub.application.service.CreateDocumentCollectionService;
import me.joohyuk.datahub.domain.entity.DocumentCollection;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import com.spartaecommerce.domain.vo.CollectionId;
import me.joohyuk.datahub.fake.FakeDateTimeHolder;
import me.joohyuk.datahub.fake.InMemoryDocumentCollectionRepository;
import me.joohyuk.datahub.fake.InMemoryIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CreateDocumentCollectionService 테스트")
class CreateDocumentCollectionServiceTest {

  private CreateDocumentCollectionService service;
  private InMemoryDocumentCollectionRepository repository;
  private InMemoryIdGenerator idGenerator;
  private FakeDateTimeHolder dateTimeHolder;

  @BeforeEach
  void setUp() {
    // Given: 실제 객체 그래프 구성 (Classical School)
    idGenerator = new InMemoryIdGenerator();
    dateTimeHolder = new FakeDateTimeHolder();
    repository = new InMemoryDocumentCollectionRepository();
    service = new CreateDocumentCollectionService(idGenerator, dateTimeHolder, repository);
  }

  @Nested
  @DisplayName("createCollection 메서드는")
  class CreateCollectionTests {

    @Test
    @DisplayName("유효한 명령이 제공되면 컬렉션을 성공적으로 생성한다")
    void should_create_collection_successfully_when_valid_command_provided() {
      // Given: 유효한 컬렉션 생성 명령
      CreateDocumentCollectionCommand command = createCollectionCommand(
          "AI Research Papers",
          "Collection of research papers about AI",
          1L
      );

      // When: 컬렉션 생성 요청
      CreateDocumentCollectionResult result = service.createCollection(new UserId(1L), command);

      // Then: 출력 기반 검증 - 반환된 결과가 올바른가?
      assertThat(result).isNotNull();
      assertThat(result.collectionId()).isNotBlank();
      assertThat(result.name()).isEqualTo("AI Research Papers");
      assertThat(result.description()).isEqualTo("Collection of research papers about AI");
      assertThat(result.createdAt()).isNotNull();
      assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());

      // And: 상태 기반 검증 - 저장소에 올바르게 저장되었는가?
      CollectionId savedCollectionId = CollectionId.of(result.collectionId());
      Optional<DocumentCollection> savedCollection = repository.findById(savedCollectionId);

      assertThat(savedCollection).isPresent();
      assertThat(savedCollection.get().getName()).isEqualTo("AI Research Papers");
      assertThat(savedCollection.get().getDescription()).isEqualTo(
          "Collection of research papers about AI");
    }

    @Test
    @DisplayName("설명이 제공되지 않으면 null 설명으로 컬렉션을 생성한다")
    void should_create_collection_with_null_description_when_description_not_provided() {
      // Given: 설명이 null인 컬렉션 생성 명령
      CreateDocumentCollectionCommand command = createCollectionCommand(
          "Simple Collection",
          null,
          1L
      );

      // When: 컬렉션 생성 요청
      CreateDocumentCollectionResult result = service.createCollection(new UserId(1L), command);

      // Then: 출력 기반 검증 - 설명이 null로 저장되는가?
      assertThat(result).isNotNull();
      assertThat(result.name()).isEqualTo("Simple Collection");
      assertThat(result.description()).isNull();

      // And: 상태 기반 검증
      CollectionId savedCollectionId = CollectionId.of(result.collectionId());
      Optional<DocumentCollection> savedCollection = repository.findById(savedCollectionId);

      assertThat(savedCollection).isPresent();
      assertThat(savedCollection.get().getDescription()).isNull();
    }

    @Test
    @DisplayName("컬렉션 이름이 이미 존재하면 예외를 발생시킨다")
    void should_throw_exception_when_collection_name_already_exists() {
      // Given: 이미 존재하는 컬렉션 이름
      String duplicateName = "Existing Collection";
      Instant now = dateTimeHolder.now();
      DocumentCollection existingCollection = DocumentCollection.of(
          idGenerator.generateId(),
          duplicateName,
          "Original description",
          now,
          now
      );
      repository.save(existingCollection);

      CreateDocumentCollectionCommand command = createCollectionCommand(
          duplicateName,
          "New description",
          2L
      );

      // When & Then: 중복된 이름으로 생성 시도하면 예외 발생
      assertThatThrownBy(() -> service.createCollection(new UserId(2L), command))
          .isInstanceOf(DatahubDomainException.class)
          .hasMessageContaining("Collection with name '" + duplicateName + "' already exists");

      // And: 상태 기반 검증 - 기존 컬렉션만 존재하고 새 컬렉션은 저장되지 않음
      List<DocumentCollection> allCollections = repository.findAll();
      assertThat(allCollections).hasSize(1);
      assertThat(allCollections.getFirst().getDescription()).isEqualTo("Original description");
    }

    @Test
    @DisplayName("컬렉션 생성 시 고유한 컬렉션 ID를 생성한다")
    void should_generate_unique_collection_id_when_creating_collection() {
      // Given: 여러 컬렉션 생성 명령
      CreateDocumentCollectionCommand command1 = createCollectionCommand(
          "Collection 1",
          "First collection",
          1L
      );
      CreateDocumentCollectionCommand command2 = createCollectionCommand(
          "Collection 2",
          "Second collection",
          1L
      );

      // When: 여러 컬렉션 생성
      CreateDocumentCollectionResult result1 = service.createCollection(new UserId(1L), command1);
      CreateDocumentCollectionResult result2 = service.createCollection(new UserId(1L), command2);

      // Then: 출력 기반 검증 - 각 컬렉션이 고유한 ID를 가지는가?
      assertThat(result1.collectionId()).isNotEqualTo(result2.collectionId());

      // And: 상태 기반 검증 - 두 컬렉션 모두 저장됨
      assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("컬렉션 생성 시 타임스탬프를 올바르게 설정한다")
    void should_set_timestamps_correctly_when_creating_collection() {
      // Given: 컬렉션 생성 명령
      Instant beforeCreation = Instant.now();
      CreateDocumentCollectionCommand command = createCollectionCommand(
          "Time Test Collection",
          "Testing timestamps",
          1L
      );

      // When: 컬렉션 생성
      CreateDocumentCollectionResult result = service.createCollection(new UserId(1L), command);
      Instant afterCreation = Instant.now();

      // Then: 출력 기반 검증 - 생성 시간이 올바른 범위에 있는가?
      assertThat(result.createdAt())
          .isAfterOrEqualTo(beforeCreation)
          .isBeforeOrEqualTo(afterCreation);

      // And: 상태 기반 검증 - 엔티티의 타임스탬프도 올바른가?
      CollectionId collectionId = CollectionId.of(result.collectionId());
      DocumentCollection savedCollection = repository.findById(collectionId).orElseThrow();

      assertThat(savedCollection.getCreatedAt()).isEqualTo(savedCollection.getUpdatedAt());
      assertThat(savedCollection.getCreatedAt())
          .isAfterOrEqualTo(beforeCreation)
          .isBeforeOrEqualTo(afterCreation);
    }
  }

  // ===========================
  // Test Helper Methods
  // ===========================

  /**
   * 테스트용 CreateDocumentCollectionCommand 생성 헬퍼
   * <p>
   * 테스트 데이터의 의도를 명확히 하고 중복을 줄입니다.
   */
  private CreateDocumentCollectionCommand createCollectionCommand(
      String name,
      String description,
      Long userId  // createCollection(UserId, command) 호출 시 사용되지만 command 자체에는 포함되지 않음
  ) {
    return new CreateDocumentCollectionCommand(name, description);
  }
}