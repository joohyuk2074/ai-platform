package me.joohyuk.datahub.application.dto.response;

import me.joohyuk.datahub.domain.entity.DocumentCollection;

import java.time.Instant;

/**
 * 문서 컬렉션 생성 결과
 *
 * DocumentCollection 생성 후 반환되는 응답 정보를 담는 DTO입니다.
 */
public record CreateDocumentCollectionResult(
    String collectionId,      // 생성된 컬렉션 ID
    String name,              // 컬렉션 이름
    String description,       // 컬렉션 설명
    Instant createdAt         // 생성 시간
) {

  /**
   * DocumentCollection 엔티티로부터 응답 DTO를 생성합니다.
   */
  public static CreateDocumentCollectionResult from(DocumentCollection collection) {
    return new CreateDocumentCollectionResult(
        String.valueOf(collection.getId().getValue()),
        collection.getName(),
        collection.getDescription(),
        collection.getCreatedAt()
    );
  }
}
