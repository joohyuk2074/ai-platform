package me.joohyuk.datahub.infrastructure.adapter;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import me.joohyuk.datahub.domain.vo.DocumentStatus;

@Getter
@Builder
public class PassageCreationRequestAvroModel {

  private final String id;
  private final String sagaId;
  private final String userId;
  private final String collectionId;
  private final String metadata;
  private final Instant createdAt;
  private final DocumentStatus documentStatus;
}
