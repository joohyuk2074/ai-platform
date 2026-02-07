package me.joohyuk.datahub.application.service;

import com.spartaecommerce.domain.vo.CollectionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.port.in.service.DeleteDocumentCollectionUseCase;
import me.joohyuk.datahub.application.port.out.persistence.DocumentCollectionRepository;
import me.joohyuk.datahub.domain.entity.DocumentCollection;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.exception.DatahubDomainErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DeleteDocumentCollectionService implements DeleteDocumentCollectionUseCase {

  private final DocumentCollectionRepository documentCollectionRepository;

  @Override
  public void deleteCollection(CollectionId collectionId) {
    DocumentCollection collection = documentCollectionRepository.findById(collectionId)
        .orElseThrow(() -> new DatahubDomainException(
            "Collection not found with ID: " + collectionId,
            DatahubDomainErrorCode.DOCUMENT_COLLECTION_NOT_FOUND
        ));

    // TODO: collectionId에 해당하는 Document가 조회되는지 여부에따라서 삭제요청하도록 수정

    documentCollectionRepository.delete(collectionId);

    log.info("Collection deleted successfully with ID: {}", collectionId);
  }
}
