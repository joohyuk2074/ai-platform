package com.spartaecommerce.domain.port;

/**
 * ID 생성을 위한 포트 인터페이스
 * Hexagonal Architecture의 아웃바운드 포트로, 도메인이 인프라에 의존하지 않도록 추상화
 */
public interface IdGenerator {

  /**
   * 분산 환경에서 유일한 Long 타입 ID 생성
   *
   * @return 생성된 고유 ID
   */
  long generateId();
}
