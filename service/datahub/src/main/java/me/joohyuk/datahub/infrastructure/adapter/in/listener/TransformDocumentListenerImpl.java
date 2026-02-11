package me.joohyuk.datahub.infrastructure.adapter.in.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.service.PassageCreationSaga;
import me.joohyuk.datahub.domain.entity.PassageResponse;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;
import me.joohyuk.datahub.application.port.in.listener.TransformDocumentListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class TransformDocumentListenerImpl implements TransformDocumentListener {

  private final PassageCreationSaga passageCreationSaga;

  @Override
  public void onCompleted(PassageResponse passageResponse) {
    TransformDocumentEvent domainEvent = passageCreationSaga.process(passageResponse);
    log.info("Document transformed for documentId: {}",
        passageResponse.getDocumentId());
  }

  @Override
  public void onFailed(PassageResponse passageResponse) {
    passageCreationSaga.rollback(passageResponse);
    log.info("Passage creation is roll backed for documentId: {} with failure messages: {}",
        passageResponse.getDocumentId(),
        String.join(",", passageResponse.getFailureMessages()));
  }
}
