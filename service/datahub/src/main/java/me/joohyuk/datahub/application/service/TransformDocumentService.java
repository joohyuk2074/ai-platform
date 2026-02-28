package me.joohyuk.datahub.application.service;

import com.spartaecommerce.domain.vo.CollectionId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.port.in.service.TransformDocumentUseCase;
import me.joohyuk.datahub.application.service.handler.TransformDocumentHandler;
import me.joohyuk.datahub.application.service.handler.TransformDocumentOutboxHandler;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformDocumentService implements TransformDocumentUseCase {

  private final TransformDocumentHandler transformDocumentHandler;
  private final TransformDocumentOutboxHandler transformDocumentOutboxHandler;

  @Transactional
  public void transform(CollectionId collectionId) {
    List<TransformDocumentEvent> events =
        transformDocumentHandler.processTransformRequest(collectionId);

    if (events.isEmpty()) {
      return;
    }

    transformDocumentOutboxHandler.saveAll(events);

    log.info("Transform request completed - collectionId: {}, total: {}",
        collectionId.getValue(), events.size());
  }
}
