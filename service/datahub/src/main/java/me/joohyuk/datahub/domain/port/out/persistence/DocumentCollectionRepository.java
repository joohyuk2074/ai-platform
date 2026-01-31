package me.joohyuk.datahub.domain.port.out.persistence;

import java.util.List;
import java.util.Optional;
import me.joohyuk.datahub.domain.entity.DocumentCollection;
import me.joohyuk.datahub.domain.vo.CollectionId;

public interface DocumentCollectionRepository {

  DocumentCollection save(DocumentCollection collection);

  Optional<DocumentCollection> findById(CollectionId collectionId);

  Optional<DocumentCollection> findByName(String name);

  DocumentCollection getById(CollectionId collectionId);

  List<DocumentCollection> findAll();

  void delete(CollectionId id);

  boolean existsById(CollectionId id);

  boolean existsByName(String name);
}
