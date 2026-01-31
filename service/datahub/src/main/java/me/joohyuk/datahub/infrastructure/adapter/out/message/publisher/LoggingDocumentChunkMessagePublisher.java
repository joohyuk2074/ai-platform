package me.joohyuk.datahub.infrastructure.adapter.message.publisher;

import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.port.out.message.publisher.DocumentChunkMessagePublisher;
import org.springframework.stereotype.Component;

/**
 * 문서 업로드 이벤트를 로깅하는 Publisher 구현체
 *
 * <p>개발 환경에서 사용하기 위한 간단한 구현체입니다.
 * 프로덕션 환경에서는 Kafka, RabbitMQ 등의 메시지 브로커를 사용하는 구현체로 대체해야 합니다.</p>
 */
@Slf4j
@Component
public class LoggingDocumentChunkMessagePublisher implements DocumentChunkMessagePublisher {

  @Override
  public void publish(DocumentUploadedEvent domainEvent) {
    log.info("===== Document Uploaded Event Published =====");
    log.info("Document ID: {}", domainEvent.getDocument().getId().getValue());
    log.info("File Key: {}", domainEvent.getDocument().getFileKey());
    log.info("File Name: {}", domainEvent.getDocument().getMetadata().fileName());
    log.info("File Size: {} bytes", domainEvent.getDocument().getMetadata().fileSize());
    log.info("Content Type: {}", domainEvent.getDocument().getMetadata().contentType());
    log.info("User ID: {}", domainEvent.getDocument().getMetadata().uploadedBy().getValue());
    log.info("Created At: {}", domainEvent.getCreatedAt());
    log.info("=============================================");
  }
}
