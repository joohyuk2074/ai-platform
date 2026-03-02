package me.joohyuk.datahub.fake;

import com.spartaecommerce.domain.port.IdGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * In-Memory IdGenerator 구현
 * <p>
 * Classical School 원칙에 따라 mock이 아닌 fake 구현을 사용합니다:
 * - 실제 ID 생성기처럼 동작하는 in-memory 구현
 * - AtomicLong을 사용하여 순차적으로 증가하는 고유 ID 생성
 * - Thread-safe 보장
 * - 테스트 격리를 위해 각 테스트마다 새 인스턴스 생성
 * <p>
 * 이 방식의 장점:
 * 1. 예측 가능한 ID: 테스트에서 ID 값을 예측할 수 있어 검증이 용이
 * 2. 고유성 보장: 실제 ID 생성기처럼 중복 없는 ID 생성
 * 3. 간단한 구현: Snowflake ID 같은 복잡한 로직 없이 단순 증가
 * 4. 실제 동작 검증: Mock의 행동 검증이 아닌 실제 ID 생성 동작 검증
 */
public class InMemoryIdGenerator implements IdGenerator {

  private final AtomicLong sequence;

  public InMemoryIdGenerator() {
    this(1L);
  }

  public InMemoryIdGenerator(long initialValue) {
    this.sequence = new AtomicLong(initialValue);
  }

  @Override
  public long generateId() {
    return sequence.getAndIncrement();
  }

  /**
   * 테스트 헬퍼: 현재 시퀀스 값 조회 (다음에 생성될 ID)
   * 테스트에서 ID 값을 예측해야 할 때 사용
   */
  public long getCurrentSequence() {
    return sequence.get();
  }

  /**
   * 테스트 헬퍼: 시퀀스 리셋
   * 특정 테스트에서 ID 값을 제어해야 할 때 사용
   */
  public void reset() {
    sequence.set(1L);
  }

  /**
   * 테스트 헬퍼: 특정 값으로 시퀀스 리셋
   * 특정 테스트에서 ID 값을 제어해야 할 때 사용
   */
  public void reset(long value) {
    sequence.set(value);
  }
}
