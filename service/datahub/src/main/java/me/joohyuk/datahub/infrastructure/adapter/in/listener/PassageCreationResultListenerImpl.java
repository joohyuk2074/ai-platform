package me.joohyuk.datahub.infrastructure.adapter.in.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.PassageCreationSaga;
import me.joohyuk.datahub.domain.entity.PassageResponse;
import me.joohyuk.datahub.domain.event.PassageCreationRequestEvent;
import me.joohyuk.datahub.domain.port.in.listener.PassageCreationResultListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class PassageCreationResultListenerImpl implements PassageCreationResultListener {

  private final PassageCreationSaga passageCreationSaga;

  @Override
  public void onCompleted(PassageResponse passageResponse) {
    PassageCreationRequestEvent domainEvent = passageCreationSaga.process(passageResponse);
    log.info("Publishing PassageCreationEvent for message documentId: {}",
        passageResponse.getDocumentId());
    domainEvent.fire();
  }

  @Override
  public void onFailed(PassageResponse passageResponse) {
    passageCreationSaga.rollback(passageResponse);
    log.info("Passage creation is roll backed for documentId: {} with failure messages: {}",
        passageResponse.getDocumentId(),
        String.join(",", passageResponse.getFailureMessages()));
  }
}
