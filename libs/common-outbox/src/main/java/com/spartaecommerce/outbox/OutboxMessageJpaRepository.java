package com.spartaecommerce.outbox;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 아웃박스 메시지의 Spring Data JPA Repository입니다.
 *
 * <p>이 인터페이스는 아웃박스 패턴의 인프라 구현 세부사항이며, 도메인 포트로 노출되지 않습니다.
 * {@link OutboxMessagePublisher}와 {@link OutboxMessageScheduler}에서만 사용됩니다.
 */
public interface OutboxMessageJpaRepository extends JpaRepository<OutboxMessageJpaEntity, Long> {

  /**
   * 지정된 상태의 아웃박스 메시지를 생성 시간 오름차순으로 조회합니다.
   *
   * <p>스케줄러에서 {@link OutboxMessageStatus#PENDING} 메시지를 폴링할 때 사용됩니다.
   *
   * @param status 조회할 상태
   * @return 조건에 맞는 메시지 목록 (생성 시간 오름차순)
   */
  List<OutboxMessageJpaEntity> findByStatusOrderByCreatedAt(OutboxMessageStatus status);
}
