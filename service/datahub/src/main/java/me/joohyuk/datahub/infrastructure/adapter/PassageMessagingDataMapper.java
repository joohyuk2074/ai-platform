package me.joohyuk.datahub.infrastructure.adapter;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;
import me.joohyuk.datahub.infrastructure.adapter.out.persistence.converter.MetadataConverter;
import me.joohyuk.messaging.events.DocumentTransformRequestedMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PassageMessagingDataMapper {

  private final MetadataConverter metadataConverter;

  public DocumentTransformRequestedMessage eventToMessage(TransformDocumentEvent event) {
    DocumentTransformRequestedMessage.Document document = new DocumentTransformRequestedMessage.Document(
        event.getDocumentId(),
        event.getCollectionId(),
        event.getFileKey(),
        event.getContentHash(),
        event.getMetadata(),
        event.getTrackingId(),
        event.getStatus(),
        event.getAttempt(),
        event.getLastErrorCode(),
        event.getLastErrorMessage(),
        event.getPassageCount(),
        event.getLastResultEventId(),
        event.getDocumentCreatedAt(),
        event.getDocumentUpdatedAt(),
        event.getUploadedBy()
    );

    return new DocumentTransformRequestedMessage(document, event.getEventCreatedAt());
  }

  public PassageCreationRequestAvroModel passageCreationRequestEventToPassageCreationRequestAvroModel(
      TransformDocumentEvent event
  ) {
    return PassageCreationRequestAvroModel.builder()
        .id(UUID.randomUUID().toString())
        .sagaId("")
        .userId(event.getUploadedBy().toString())
        .collectionId(event.getCollectionId().toString())
        .metadata(metadataConverter.convertToDatabaseColumn(event.getMetadata()))
        .createdAt(event.getEventCreatedAt())
        .documentStatus(me.joohyuk.datahub.domain.vo.DocumentStatus.valueOf(event.getStatus()))
        .build();
  }
}
