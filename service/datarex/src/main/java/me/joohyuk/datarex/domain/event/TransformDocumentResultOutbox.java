package me.joohyuk.datarex.domain.event;

import com.spartaecommerce.outbox.OutboxStatus;
import java.time.LocalDateTime;

public record TransformDocumentResultOutbox(
    Long id,
    Long sagaId,
    OutboxStatus outboxStatus,
    String payload,
    LocalDateTime createdAt,
    LocalDateTime processedAt
) {

}
