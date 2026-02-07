package me.joohyuk.datahub.fake;

import com.spartaecommerce.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import me.joohyuk.datahub.domain.entity.DocumentCollection;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentCollectionRepository;
import com.spartaecommerce.domain.vo.CollectionId;

/**
 * In-Memory DocumentCollectionRepository 구현
 * <p>
 * Classical School 원칙에 따라 mock이 아닌 fake 구현을 사용합니다: - 실제 저장소처럼 동작하는 in-memory 구현 - 비즈니스 로직 없이 순수한
 * 데이터 저장/조회만 담당 - Thread-safe를 위해 ConcurrentHashMap 사용 - 테스트 격리를 위해 각 테스트마다 새 인스턴스 생성
 * <p>
 * 이 방식의 장점: 1. 리팩토링 내성: 내부 구현이 변경되어도 테스트는 영향받지 않음 2. 빠른 피드백: In-memory 구현으로 빠른 테스트 실행 3. 실제 동작 검증:
 * Mock의 행동 검증이 아닌 실제 상태 변화 검증 4. 명확한 의도: 저장소가 어떻게 동작해야 하는지 명확히 표현
 */
public class InMemoryDocumentCollectionRepository implements DocumentCollectionRepository {

  private final Map<CollectionId, DocumentCollection> store = new ConcurrentHashMap<>();

  @Override
  public DocumentCollection save(DocumentCollection collection) {
    Objects.requireNonNull(collection, "Collection cannot be null");
    Objects.requireNonNull(collection.getId(), "Collection ID cannot be null");

    // 엔티티를 저장소에 저장 (실제 저장소처럼 동일한 인스턴스 반환)
    store.put(collection.getId(), collection);
    return collection;
  }

  @Override
  public DocumentCollection getById(CollectionId collectionId) {
    DocumentCollection documentCollection = store.get(collectionId);
    if (documentCollection == null) {
      throw new DatahubDomainException(ErrorCode.ENTITY_NOT_FOUND.getMessage());
    }
    return documentCollection;
  }

  @Override
  public Optional<DocumentCollection> findById(CollectionId id) {
    Objects.requireNonNull(id, "Collection ID cannot be null");
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Optional<DocumentCollection> findByName(String name) {
    Objects.requireNonNull(name, "Collection name cannot be null");
    return store.values().stream()
        .filter(collection -> collection.getName().equals(name))
        .findFirst();
  }

  @Override
  public List<DocumentCollection> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public void delete(CollectionId id) {
    Objects.requireNonNull(id, "Collection ID cannot be null");
    store.remove(id);
  }

  @Override
  public boolean existsById(CollectionId id) {
    Objects.requireNonNull(id, "Collection ID cannot be null");
    return store.containsKey(id);
  }

  @Override
  public boolean existsByName(String name) {
    Objects.requireNonNull(name, "Collection name cannot be null");
    return store.values().stream()
        .anyMatch(collection -> collection.getName().equals(name));
  }

  /**
   * 테스트 헬퍼: 저장소 초기화 테스트 격리가 필요한 경우 사용
   */
  public void clear() {
    store.clear();
  }

  /**
   * 테스트 헬퍼: 저장된 컬렉션 수 반환 테스트 검증에 유용
   */
  public int size() {
    return store.size();
  }
}
