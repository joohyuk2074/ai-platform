package me.joohyuk.ingestion.domain.repository;

import me.joohyuk.ingestion.domain.vo.CollectionId;
import me.joohyuk.ingestion.domain.entity.Document;
import me.joohyuk.ingestion.domain.vo.DocumentId;
import me.joohyuk.ingestion.domain.vo.DocumentStatus;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository {

    Document save(Document document);

    Optional<Document> findById(DocumentId id);

    List<Document> findByCollectionId(CollectionId collectionId);

    List<Document> findByStatus(DocumentStatus status);

    void delete(DocumentId id);

    boolean existsById(DocumentId id);

    List<Document> findAll();
}
