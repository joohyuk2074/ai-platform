package me.joohyuk.datahub.application.port.out.persistence;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.ContentHash;
import com.spartaecommerce.domain.vo.DocumentId;
import java.util.List;
import java.util.Optional;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.vo.DocumentStatus;

public interface DocumentRepository {

  Document save(Document document);

  Optional<Document> findById(DocumentId documentId);

  Document getById(DocumentId documentId);

  List<Document> findAll();

  List<Document> findByFileKey(String fileKey);

  /**
   * 특정 컬렉션에 속하는 문서 목록을 조회합니다.
   *
   * @param collectionId 조회할 컬렉션 ID
   * @return 해당 컬렉션의 문서 목록 (빈 리스트 가능)
   */
  List<Document> findByCollectionId(CollectionId collectionId);

  List<Document> findByCollectionId(CollectionId collectionId, DocumentStatus documentStatus);

  void delete(DocumentId id);

  boolean existsById(DocumentId id);

  boolean existsByFileKey(String fileKey);

  boolean existsByContentHash(ContentHash contentHash);

  void saveAll(List<Document> documents);
}
