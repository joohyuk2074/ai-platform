package me.joohyuk.datahub.infrastructure.adapter.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.joohyuk.datahub.domain.entity.DocumentCollection;
import me.joohyuk.datahub.domain.exception.IngestionDomainException;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentCollectionRepository;
import me.joohyuk.datahub.domain.vo.CollectionId;
import me.joohyuk.datahub.infrastructure.adapter.persistence.entity.DocumentCollectionJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DocumentCollectionRepositoryImpl implements DocumentCollectionRepository {

  private final DocumentCollectionJpaRepository jpaRepository;

  @Override
  public DocumentCollection save(DocumentCollection collection) {
    DocumentCollectionJpaEntity jpaEntity = DocumentCollectionJpaEntity.from(collection);
    DocumentCollectionJpaEntity savedEntity = jpaRepository.save(jpaEntity);
    return savedEntity.toDomain();
  }

  @Override
  public Optional<DocumentCollection> findById(CollectionId id) {
    return jpaRepository.findById(id.getValue())
        .map(DocumentCollectionJpaEntity::toDomain);
  }

  @Override
  public Optional<DocumentCollection> findByName(String name) {
    return jpaRepository.findByName(name)
        .map(DocumentCollectionJpaEntity::toDomain);
  }

  @Override
  public DocumentCollection getById(CollectionId collectionId) {
    return jpaRepository.findById(collectionId.getValue())
        .map(DocumentCollectionJpaEntity::toDomain)
        .orElseThrow(
            () -> new IngestionDomainException("Collection not found with ID: " + collectionId));
  }

  @Override
  public List<DocumentCollection> findAll() {
    return jpaRepository.findAll().stream()
        .map(DocumentCollectionJpaEntity::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void delete(CollectionId id) {
    jpaRepository.deleteById(id.getValue());
  }

  @Override
  public boolean existsById(CollectionId id) {
    return jpaRepository.existsById(id.getValue());
  }

  @Override
  public boolean existsByName(String name) {
    return jpaRepository.existsByName(name);
  }
}
