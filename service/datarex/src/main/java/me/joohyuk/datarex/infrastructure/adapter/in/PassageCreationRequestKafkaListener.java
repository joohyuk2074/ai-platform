package me.joohyuk.datarex.infrastructure.adapter.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.application.service.PassageCreationServiceImpl;
import me.joohyuk.datarex.domain.entity.PassageCreationRequestedMessage;
import me.joohyuk.datarex.domain.port.in.listener.PassageCreationRequestMessageListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PassageCreationRequestKafkaListener implements PassageCreationRequestMessageListener {

    private final PassageCreationServiceImpl passageCreationService;

    @KafkaListener(
        topics = "passage.creation.requested",
        groupId = "datarex-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Override
    public void onMessage(PassageCreationRequestedMessage message) {
        passageCreationService.createPassages(message);
    }
}
