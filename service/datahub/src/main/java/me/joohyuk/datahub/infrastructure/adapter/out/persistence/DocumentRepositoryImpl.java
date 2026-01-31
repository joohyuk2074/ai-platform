package me.joohyuk.datahub.infrastructure.adapter.out.persistence;

import com.spartaecommerce.domain.vo.DocumentId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.exception.IngestionDomainException;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentRepository;
import me.joohyuk.datahub.domain.vo.ContentHash;
import me.joohyuk.datahub.infrastructure.adapter.out.persistence.entity.DocumentJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DocumentRepositoryImpl implements DocumentRepository {

  private final DocumentJpaRepository jpaRepository;

  @Override
  public Document save(Document document) {
    DocumentJpaEntity jpaEntity = DocumentJpaEntity.from(document);
    DocumentJpaEntity savedEntity = jpaRepository.save(jpaEntity);
    return savedEntity.toDomain();
  }

  @Override
  public Optional<Document> findById(DocumentId documentId) {
    return jpaRepository.findById(documentId.getValue())
        .map(DocumentJpaEntity::toDomain);
  }

  @Override
  public Document getById(DocumentId documentId) {
    return jpaRepository.findById(documentId.getValue())
        .map(DocumentJpaEntity::toDomain)
        .orElseThrow(
            () -> new IngestionDomainException("Document not found with ID: " + documentId));
  }

  @Override
  public List<Document> findAll() {
    return jpaRepository.findAll().stream()
        .map(DocumentJpaEntity::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Document> findByFileKey(String fileKey) {
    return jpaRepository.findByFileKey(fileKey).stream()
        .map(DocumentJpaEntity::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void delete(DocumentId id) {
    jpaRepository.deleteById(id.getValue());
  }

  @Override
  public boolean existsById(DocumentId id) {
    return jpaRepository.existsById(id.getValue());
  }

  @Override
  public boolean existsByFileKey(String fileKey) {
    return jpaRepository.existsByFileKey(fileKey);
  }

  @Override
  public boolean existsByContentHash(ContentHash contentHash) {
    return jpaRepository.existsByContentHash(contentHash.getValue());
  }
}
