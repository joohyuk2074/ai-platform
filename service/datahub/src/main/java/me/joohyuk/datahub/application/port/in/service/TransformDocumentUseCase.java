package me.joohyuk.datahub.application.port.in.service;

import com.spartaecommerce.domain.vo.CollectionId;

public interface TransformDocumentUseCase {

  void transform(CollectionId collectionId);
}
