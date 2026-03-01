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
    store.put(outbox.getId(), outbox);
    saveHistory.add(outbox);
    return outbox;
  }

  @Override
  public List<TransformDocumentResultOutbox> findAllByOutboxStatus(String sagaType, OutboxStatus outboxStatus) {
    return List.of();
  }

  @Override
  public Optional<TransformDocumentResultOutbox> findByCorrelationId(String correlationId) {
    return store.values().stream()
        .filter(outbox -> correlationId.equals(outbox.getCorrelationId()))
        .findFirst();
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
   * 저장소를 초기화합니다.
   */
  public void reset() {
    store.clear();
    saveHistory.clear();
  }
}
