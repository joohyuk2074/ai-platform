package me.joohyuk.datahub.application.dto.result;

import java.time.Instant;
import me.joohyuk.datahub.domain.entity.DocumentCollection;

public record CreateDocumentCollectionResult(
    String collectionId,      // 생성된 컬렉션 ID
    String name,              // 컬렉션 이름
    String description,       // 컬렉션 설명
    Instant createdAt         // 생성 시간
) {

  public static CreateDocumentCollectionResult from(DocumentCollection collection) {
    return new CreateDocumentCollectionResult(
        String.valueOf(collection.getId().getValue()),
        collection.getName(),
        collection.getDescription(),
        collection.getCreatedAt()
    );
  }
}
