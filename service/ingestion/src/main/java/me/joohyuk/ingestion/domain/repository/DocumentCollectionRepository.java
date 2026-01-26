package me.joohyuk.ingestion.domain.repository;

import me.joohyuk.ingestion.domain.vo.CollectionId;
import me.joohyuk.ingestion.domain.entity.DocumentCollection;

import java.util.List;
import java.util.Optional;

public interface DocumentCollectionRepository {

    DocumentCollection save(DocumentCollection collection);

    Optional<DocumentCollection> findById(CollectionId id);

    Optional<DocumentCollection> findByName(String name);

    List<DocumentCollection> findAll();

    void delete(CollectionId id);

    boolean existsById(CollectionId id);

    boolean existsByName(String name);
}
