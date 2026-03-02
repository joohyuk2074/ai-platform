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
import me.joohyuk.datahub.domain.entity.DlqOutbox;

/**
 * DLQ Outbox JPA 엔티티
 */
@Builder
@Entity
@Table(name = "dlq_outbox")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DlqOutboxJpaEntity {

  @Id
  private Long id;

  @Column(nullable = false)
  private String correlationId;

  @Column(nullable = false)
  private String originalTopic;

  @Column(name = "payload", columnDefinition = "LONGTEXT", nullable = false)
  private String payload;

  @Column(nullable = false)
  private String errorCode;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  private LocalDateTime processedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OutboxStatus outboxStatus;

  @Version
  private int version;

  public static DlqOutboxJpaEntity from(DlqOutbox dlqOutbox) {
    return DlqOutboxJpaEntity.builder()
        .id(dlqOutbox.getId())
        .correlationId(dlqOutbox.getCorrelationId())
        .originalTopic(dlqOutbox.getOriginalTopic())
        .payload(dlqOutbox.getPayload())
        .errorCode(dlqOutbox.getErrorCode())
        .errorMessage(dlqOutbox.getErrorMessage())
        .createdAt(dlqOutbox.getCreatedAt())
        .processedAt(dlqOutbox.getProcessedAt())
        .outboxStatus(dlqOutbox.getOutboxStatus())
        .version(dlqOutbox.getVersion())
        .build();
  }

  public DlqOutbox toDomain() {
    return DlqOutbox.builder()
        .id(this.id)
        .correlationId(this.correlationId)
        .originalTopic(this.originalTopic)
        .payload(this.payload)
        .errorCode(this.errorCode)
        .errorMessage(this.errorMessage)
        .createdAt(this.createdAt)
        .processedAt(this.processedAt)
        .outboxStatus(this.outboxStatus)
        .version(this.version)
        .build();
  }
}
