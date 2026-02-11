package me.joohyuk.datahub.domain.entity;

import com.spartaecommerce.outbox.OutboxStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.joohyuk.commonsaga.SagaStatus;
import me.joohyuk.datahub.domain.vo.DocumentStatus;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TransformDocumentOutbox {

  private Long id;
  private Long sagaId;

  private String type;
  private String payload;

  private SagaStatus sagaStatus;
  @Setter
  private OutboxStatus outboxStatus;

  private DocumentStatus documentStatus;

  private int version;

  private LocalDateTime createdAt;
  private LocalDateTime processedAt;

}
