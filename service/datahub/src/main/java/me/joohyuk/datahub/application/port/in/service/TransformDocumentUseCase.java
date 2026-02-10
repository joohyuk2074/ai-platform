package me.joohyuk.datahub.application.port.in.service;

import com.spartaecommerce.domain.vo.CollectionId;
import me.joohyuk.datahub.application.dto.result.TransformDocumentRequestsResult;

public interface TransformDocumentUseCase {

  TransformDocumentRequestsResult transform(CollectionId collectionId);
}
