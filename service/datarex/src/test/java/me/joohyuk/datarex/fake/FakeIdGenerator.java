package me.joohyuk.datarex.fake;

import com.spartaecommerce.domain.port.IdGenerator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 테스트용 IdGenerator 구현체
 * <p>
 * Classical Testing을 위해 예측 가능한 ID를 생성합니다.
 */
public class FakeIdGenerator implements IdGenerator {

  private final AtomicLong counter = new AtomicLong(1);

  @Override
  public long generateId() {
    return counter.getAndIncrement();
  }

  public void reset() {
    counter.set(1);
  }

  public void setNextId(long nextId) {
    counter.set(nextId);
  }
}
