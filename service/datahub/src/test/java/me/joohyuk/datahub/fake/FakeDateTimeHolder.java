package me.joohyuk.datahub.fake;

import com.spartaecommerce.util.DateTimeHolder;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 테스트용 DateTimeHolder 구현체
 * <p>
 * Classical Testing을 위해 시간을 제어 가능하게 만듭니다.
 * 기본적으로는 실제 시간을 반환하며, 필요시 고정 시간을 설정할 수 있습니다.
 */
public class FakeDateTimeHolder implements DateTimeHolder {

  private Instant fixedInstant;
  private LocalDateTime fixedLocalDateTime;
  private boolean useFixedTime = false;

  public FakeDateTimeHolder() {
    // 기본적으로는 실제 시간 사용
  }

  public FakeDateTimeHolder(Instant fixedInstant) {
    this.fixedInstant = fixedInstant;
    this.useFixedTime = true;
  }

  @Override
  public LocalDateTime getCurrentDateTime() {
    if (useFixedTime && fixedLocalDateTime != null) {
      return fixedLocalDateTime;
    }
    return LocalDateTime.now();
  }

  @Override
  public Instant now() {
    if (useFixedTime && fixedInstant != null) {
      return fixedInstant;
    }
    return Instant.now();
  }

  public void setFixedInstant(Instant instant) {
    this.fixedInstant = instant;
    this.useFixedTime = true;
  }

  public void setFixedLocalDateTime(LocalDateTime localDateTime) {
    this.fixedLocalDateTime = localDateTime;
    this.useFixedTime = true;
  }

  public void useRealTime() {
    this.useFixedTime = false;
  }
}
