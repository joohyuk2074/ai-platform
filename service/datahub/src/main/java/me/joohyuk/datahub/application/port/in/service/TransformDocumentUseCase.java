package me.joohyuk.datahub.application.port.in.service;

import com.spartaecommerce.domain.vo.CollectionId;
import me.joohyuk.datahub.application.dto.result.TransformDocumentResult;

public interface TransformDocumentUseCase {

  TransformDocumentResult transform(CollectionId collectionId);
}
