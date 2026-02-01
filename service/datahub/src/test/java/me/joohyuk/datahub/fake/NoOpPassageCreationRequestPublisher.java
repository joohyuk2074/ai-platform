package me.joohyuk.datahub.fake;

import me.joohyuk.datahub.domain.event.PassageCreationRequestEvent;
import me.joohyuk.datahub.domain.port.out.message.publisher.PassageCreationRequestPublisher;

/**
 * 테스트용 no-op {@link PassageCreationRequestPublisher} Fake 구현체입니다.
 *
 * <p>publish 호출을 무시하여 아웃박스 저장 및 Kafka 발행 없이 테스트할 수 있게 합니다.
 */
public class NoOpPassageCreationRequestPublisher implements PassageCreationRequestPublisher {

  @Override
  public void publish(PassageCreationRequestEvent event) {
    // no-op: 테스트에서 아웃박스 저장 불필요
  }
}
