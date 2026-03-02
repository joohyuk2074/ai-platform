package me.joohyuk.datahub.application.port.in.service;

import com.spartaecommerce.domain.vo.CollectionId;

public interface DeleteDocumentCollectionUseCase {

  void deleteCollection(CollectionId collectionId);
}
