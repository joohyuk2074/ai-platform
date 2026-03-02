package me.joohyuk.datahub.infrastructure.adapter.util;

import com.spartaecommerce.util.DateTimeHolder;
import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class SystemDateTimeHolder implements DateTimeHolder {

  @Override
  public LocalDateTime getCurrentDateTime() {
    return LocalDateTime.now();
  }

  @Override
  public Instant now() {
    return Instant.now();
  }
}
