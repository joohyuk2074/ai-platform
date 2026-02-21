package me.joohyuk.datarex.fake;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.joohyuk.datarex.application.port.out.peresistence.TransformDocumentResultOutboxRepository;
import me.joohyuk.datarex.domain.event.TransformDocumentResultOutbox;

/**
 * 테스트용 TransformDocumentResultOutboxRepository 구현체
 * <p>
 * Classical Testing을 위해 인메모리 저장소를 제공합니다.
 */
public class FakeTransformDocumentResultOutboxRepository implements
    TransformDocumentResultOutboxRepository {

  private final Map<Long, TransformDocumentResultOutbox> store = new HashMap<>();
  private final List<TransformDocumentResultOutbox> saveHistory = new ArrayList<>();

  @Override
  public TransformDocumentResultOutbox save(TransformDocumentResultOutbox outbox) {
    store.put(outbox.id(), outbox);
    saveHistory.add(outbox);
    return outbox;
  }

  @Override
  public List<TransformDocumentResultOutbox> findAllByOutboxStatus(OutboxStatus outboxStatus) {
    return store.values().stream()
        .filter(outbox -> outbox.outboxStatus().equals(outboxStatus))
        .toList();
  }

  /**
   * findById는 인터페이스에 정의되어 있지 않지만 테스트에서 유용하게 사용할 수 있습니다.
   */
  public Optional<TransformDocumentResultOutbox> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  /**
   * 모든 저장된 outbox를 반환합니다.
   */
  public List<TransformDocumentResultOutbox> findAll() {
    return new ArrayList<>(store.values());
  }

  /**
   * 저장 이력을 반환합니다 (순서 보장).
   */
  public List<TransformDocumentResultOutbox> getSaveHistory() {
    return new ArrayList<>(saveHistory);
  }

  /**
   * 저장된 outbox 개수를 반환합니다.
   */
  public int count() {
    return store.size();
  }

  /**
   * 특정 sagaId로 outbox를 찾습니다.
   */
  public Optional<TransformDocumentResultOutbox> findBySagaId(Long sagaId) {
    return store.values().stream()
        .filter(outbox -> outbox.sagaId().equals(sagaId))
        .findFirst();
  }

  /**
   * 저장소를 초기화합니다.
   */
  public void reset() {
    store.clear();
    saveHistory.clear();
  }
}
