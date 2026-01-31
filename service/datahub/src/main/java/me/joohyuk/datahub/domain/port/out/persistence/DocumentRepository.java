package me.joohyuk.datahub.domain.port.out.persistence;

import com.spartaecommerce.domain.vo.DocumentId;
import java.util.List;
import java.util.Optional;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.vo.ContentHash;

public interface DocumentRepository {

  Document save(Document document);

  Optional<Document> findById(DocumentId documentId);

  Document getById(DocumentId documentId);

  List<Document> findAll();

  List<Document> findByFileKey(String fileKey);

  void delete(DocumentId id);

  boolean existsById(DocumentId id);

  boolean existsByFileKey(String fileKey);

  boolean existsByContentHash(ContentHash contentHash);
}
