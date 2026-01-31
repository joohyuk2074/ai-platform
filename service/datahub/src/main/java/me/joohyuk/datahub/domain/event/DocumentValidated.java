package me.joohyuk.datahub.domain.event;

import com.spartaecommerce.domain.event.DomainEvent;
import com.spartaecommerce.domain.vo.DocumentId;

import java.time.Instant;

/**
 * 문서 검수 완료 이벤트
 *
 * 문서가 성공적으로 검수되었을 때 발행됩니다.
 * 후속 처리(청킹)를 트리거합니다.
 */
public record DocumentValidated(
    DocumentId documentId,
    double validationScore,
    Instant occurredAt
) implements DomainEvent<DocumentValidated> {

    public DocumentValidated {
        if (documentId == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred at cannot be null");
        }
    }

    public static DocumentValidated of(DocumentId documentId, double validationScore) {
        return new DocumentValidated(documentId, validationScore, Instant.now());
    }
}
