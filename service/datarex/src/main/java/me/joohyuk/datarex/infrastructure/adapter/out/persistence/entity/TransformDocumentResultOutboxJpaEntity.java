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

  public static TransformDocumentResultOutboxJpaEntity from(TransformDocumentResultOutbox domain) {
    return TransformDocumentResultOutboxJpaEntity.builder()
        .id(domain.id())
        .sagaId(domain.sagaId())
        .outboxStatus(domain.outboxStatus())
        .payload(domain.payload())
        .createdAt(domain.createdAt())
        .processedAt(domain.processedAt())
        .build();
  }

  public TransformDocumentResultOutbox toDomain() {
    return new TransformDocumentResultOutbox(
        id,
        sagaId,
        outboxStatus,
        payload,
        createdAt,
        processedAt
    );
  }
}
