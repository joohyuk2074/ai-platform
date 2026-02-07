package me.joohyuk.datahub.fake;

import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentRepository;
import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.ContentHash;
import me.joohyuk.datahub.domain.vo.DocumentStatus;

/**
 * In-Memory DocumentRepository 구현
 *
 * <p>Classical School 원칙에 따라 mock이 아닌 fake 구현을 사용합니다:
 * <ul>
 *   <li>실제 저장소처럼 동작하는 in-memory 구현</li>
 *   <li>테스트용 save() 실패 시뮬레이션 기능 제공 (보상 트랜잭션 테스트용)</li>
 * </ul>
 */
public class InMemoryDocumentRepository implements DocumentRepository {

  private final Map<DocumentId, Document> store = new ConcurrentHashMap<>();

  private boolean throwOnSave = false;
  private RuntimeException saveException;

  @Override
  public Document save(Document document) {
    if (throwOnSave) {
      throw saveException != null
          ? saveException
          : new RuntimeException("Simulated DB save failure");
    }
    Objects.requireNonNull(document, "Document cannot be null");
    Objects.requireNonNull(document.getId(), "Document ID cannot be null");
    store.put(document.getId(), document);
    return document;
  }

  @Override
  public Optional<Document> findById(DocumentId documentId) {
    return Optional.ofNullable(store.get(documentId));
  }

  @Override
  public Document getById(DocumentId documentId) {
    Document doc = store.get(documentId);
    if (doc == null) {
      throw new DatahubDomainException(ErrorCode.ENTITY_NOT_FOUND.getMessage());
    }
    return doc;
  }

  @Override
  public List<Document> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public List<Document> findByFileKey(String fileKey) {
    return store.values().stream()
        .filter(d -> d.getFileKey().equals(fileKey))
        .toList();
  }

  @Override
  public List<Document> findByCollectionId(CollectionId collectionId) {
    return store.values().stream()
        .filter(d -> d.getCollectionId().equals(collectionId))
        .toList();
  }

  @Override
  public List<Document> findByCollectionId(
      CollectionId collectionId,
      DocumentStatus documentStatus
  ) {
    return store.values().stream()
        .filter(d -> d.getCollectionId().equals(collectionId))
        .filter(document -> document.getStatus() == documentStatus)
        .toList();
  }

  @Override
  public void delete(DocumentId id) {
    store.remove(id);
  }

  @Override
  public boolean existsById(DocumentId id) {
    return store.containsKey(id);
  }

  @Override
  public boolean existsByFileKey(String fileKey) {
    return store.values().stream().anyMatch(d -> d.getFileKey().equals(fileKey));
  }

  @Override
  public boolean existsByContentHash(ContentHash contentHash) {
    return store.values().stream().anyMatch(d -> d.getContentHash().equals(contentHash));
  }

  // ─── 테스트 헬퍼 ──────────────────────────────────────────────

  /**
   * 현재 저장된 문서 수
   */
  public int size() {
    return store.size();
  }

  /**
   * save() 호출 시 예외를 발생시키도록 설정 (DB 저장 실패 테스트용)
   */
  public void configureToFailOnSave() {
    this.throwOnSave = true;
  }

  /**
   * save() 호출 시 특정 예외를 발생시키도록 설정
   */
  public void configureToFailOnSave(RuntimeException exception) {
    this.throwOnSave = true;
    this.saveException = exception;
  }
}
