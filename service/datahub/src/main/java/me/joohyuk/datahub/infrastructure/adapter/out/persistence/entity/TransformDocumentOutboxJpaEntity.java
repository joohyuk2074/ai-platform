package me.joohyuk.datahub.infrastructure.adapter.out.persistence.entity;

import com.spartaecommerce.outbox.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;
import me.joohyuk.datahub.domain.vo.DocumentStatus;

@Builder
@Entity
@Table(name = "transform_outbox")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransformDocumentOutboxJpaEntity {

  @Id
  private Long id;

  private String correlationId;
  private String type;

  @Column(name = "payload", columnDefinition = "LONGTEXT")
  private String payload;

  private LocalDateTime createdAt;
  private LocalDateTime processedAt;

  @Enumerated(EnumType.STRING)
  private OutboxStatus outboxStatus;

  @Enumerated(EnumType.STRING)
  private DocumentStatus documentStatus;

  @Version
  private int version;

  public static TransformDocumentOutboxJpaEntity from(
      TransformDocumentOutbox transformDocumentOutbox
  ) {
    return TransformDocumentOutboxJpaEntity.builder()
        .id(transformDocumentOutbox.getId())
        .correlationId(transformDocumentOutbox.getCorrelationId())
        .type(transformDocumentOutbox.getType())
        .payload(transformDocumentOutbox.getPayload())
        .createdAt(transformDocumentOutbox.getCreatedAt())
        .processedAt(transformDocumentOutbox.getProcessedAt())
        .outboxStatus(transformDocumentOutbox.getOutboxStatus())
        .documentStatus(transformDocumentOutbox.getDocumentStatus())
        .version(transformDocumentOutbox.getVersion())
        .build();
  }

  public TransformDocumentOutbox toDomain() {
    return TransformDocumentOutbox.builder()
        .id(this.id)
        .correlationId(this.correlationId)
        .type(this.type)
        .payload(this.payload)
        .createdAt(this.createdAt)
        .processedAt(this.processedAt)
        .outboxStatus(this.outboxStatus)
        .documentStatus(this.documentStatus)
        .version(this.version)
        .build();
  }
}
