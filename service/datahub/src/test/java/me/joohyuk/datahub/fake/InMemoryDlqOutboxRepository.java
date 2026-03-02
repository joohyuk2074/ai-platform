package me.joohyuk.datahub.fake;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import me.joohyuk.datahub.application.port.out.persistence.DlqOutboxRepository;
import me.joohyuk.datahub.domain.entity.DlqOutbox;

/**
 * In-Memory DlqOutboxRepository 구현
 *
 * <p>Classical School 원칙에 따라 mock이 아닌 fake 구현을 사용합니다:
 * <ul>
 *   <li>실제 저장소처럼 동작하는 in-memory 구현</li>
 *   <li>비즈니스 로직 없이 순수한 데이터 저장/조회만 담당</li>
 *   <li>Thread-safe를 위해 ConcurrentHashMap 사용</li>
 *   <li>테스트 격리를 위해 각 테스트마다 새 인스턴스 생성</li>
 * </ul>
 *
 * <p>이 방식의 장점:
 * <ol>
 *   <li>리팩토링 내성: 내부 구현이 변경되어도 테스트는 영향받지 않음</li>
 *   <li>빠른 피드백: In-memory 구현으로 빠른 테스트 실행</li>
 *   <li>실제 동작 검증: Mock의 행동 검증이 아닌 실제 상태 변화 검증</li>
 *   <li>명확한 의도: 저장소가 어떻게 동작해야 하는지 명확히 표현</li>
 * </ol>
 */
public class InMemoryDlqOutboxRepository implements DlqOutboxRepository {

  private final Map<Long, DlqOutbox> store = new ConcurrentHashMap<>();

  @Override
  public DlqOutbox save(DlqOutbox dlqOutbox) {
    Objects.requireNonNull(dlqOutbox, "DlqOutbox cannot be null");
    Objects.requireNonNull(dlqOutbox.getId(), "DlqOutbox ID cannot be null");

    store.put(dlqOutbox.getId(), dlqOutbox);
    return dlqOutbox;
  }

  @Override
  public List<DlqOutbox> findAllByOutboxStatus(OutboxStatus outboxStatus) {
    Objects.requireNonNull(outboxStatus, "Outbox status cannot be null");

    return store.values().stream()
        .filter(outbox -> outboxStatus.equals(outbox.getOutboxStatus()))
        .toList();
  }

  @Override
  public void deleteByOutboxStatus(OutboxStatus outboxStatus) {
    Objects.requireNonNull(outboxStatus, "Outbox status cannot be null");

    List<Long> idsToDelete = store.values().stream()
        .filter(outbox -> outboxStatus.equals(outbox.getOutboxStatus()))
        .map(DlqOutbox::getId)
        .toList();

    idsToDelete.forEach(store::remove);
  }

  // ─── 테스트 헬퍼 ──────────────────────────────────────────────

  /**
   * 테스트 헬퍼: 저장소 초기화
   * 테스트 격리가 필요한 경우 사용
   */
  public void clear() {
    store.clear();
  }

  /**
   * 테스트 헬퍼: 저장된 outbox 수 반환
   * 테스트 검증에 유용
   */
  public int size() {
    return store.size();
  }

  /**
   * 테스트 헬퍼: 모든 outbox 조회
   * 테스트에서 상태 검증에 사용
   */
  public List<DlqOutbox> findAll() {
    return new ArrayList<>(store.values());
  }

  /**
   * 테스트 헬퍼: ID로 outbox 조회
   * 테스트에서 특정 outbox 검증에 사용
   */
  public DlqOutbox findById(Long id) {
    return store.get(id);
  }
}
