package me.joohyuk.datahub.infrastructure.adapter.util;

import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.util.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdGeneratorImpl implements IdGenerator {

  private final Snowflake snowflake = new Snowflake();

  @Override
  public long generateId() {
    return snowflake.nextId();
  }
}
