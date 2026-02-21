package me.joohyuk.datarex.infrastructure.adapter.out.persistence.entity;

import com.spartaecommerce.outbox.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.joohyuk.datarex.domain.event.TransformDocumentResultOutbox;

@Entity
@Table(name = "transform_document_result_outbox")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TransformDocumentResultOutboxJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String type;

  @Column(nullable = false)
  private Long sagaId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OutboxStatus outboxStatus;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  private LocalDateTime processedAt;

  public static TransformDocumentResultOutboxJpaEntity from(
      TransformDocumentResultOutbox domain
  ) {
    return TransformDocumentResultOutboxJpaEntity.builder()
        .id(domain.getId())
        .sagaId(domain.getSagaId())
        .type(domain.getType())
        .outboxStatus(domain.getOutboxStatus())
        .payload(domain.getPayload())
        .createdAt(domain.getCreatedAt())
        .processedAt(domain.getProcessedAt())
        .build();
  }

  public TransformDocumentResultOutbox toDomain() {
    return new TransformDocumentResultOutbox(
        this.id,
        this.sagaId,
        this.type,
        this.outboxStatus,
        this.payload,
        this.createdAt,
        this.processedAt
    );
  }
}
