package me.joohyuk.datarex.domain.event;

import com.spartaecommerce.outbox.OutboxStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
public class TransformDocumentResultOutbox {

  private Long id;
  private String correlationId;
  private String type;
  @Setter
  private OutboxStatus outboxStatus;
  private String payload;
  private LocalDateTime createdAt;
  private LocalDateTime processedAt;

}
