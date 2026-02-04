package me.joohyuk.datarex.infrastructure.adapter.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.application.service.DocumentTransformServiceImpl;
import me.joohyuk.datarex.domain.entity.DocumentTransformRequestedMessage;
import me.joohyuk.datarex.domain.port.in.listener.DocumentTransformRequestMessageListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentTransformRequestKafkaListener implements DocumentTransformRequestMessageListener {

    private final DocumentTransformServiceImpl documentTransformService;

    @KafkaListener(
        topics = "document.transform.requested",
        groupId = "datarex-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Override
    public void onMessage(DocumentTransformRequestedMessage message) {
        documentTransformService.transformDocument(message);
    }
}
