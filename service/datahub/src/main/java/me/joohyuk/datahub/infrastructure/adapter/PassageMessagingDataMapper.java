package me.joohyuk.datahub.infrastructure.adapter;

import com.spartaecommerce.domain.vo.Metadata;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;
import me.joohyuk.datahub.infrastructure.adapter.out.persistence.converter.MetadataConverter;
import me.joohyuk.messaging.events.DocumentTransformRequestedMessage;
import me.joohyuk.messaging.events.DocumentTransformRequestedMessage.DocumentTransformRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PassageMessagingDataMapper {

  private final MetadataConverter metadataConverter;

  public DocumentTransformRequestedMessage eventToMessage(TransformDocumentEvent event) {
    Document document = event.getDocument();

    DocumentTransformRequest transformRequest = new DocumentTransformRequest(
        document.getId().getValue(),
        document.getCollectionId().getValue(),
        document.getFileKey(),
        document.getContentHash().getValue(),
        new Metadata(
            document.getMetadata().fileName(),
            document.getMetadata().fileSize(),
            document.getMetadata().contentType(),
            document.getMetadata().uploadedBy(),
            document.getMetadata().source(),
            document.getMetadata().author(),
            document.getMetadata().tags()
        ),
        document.getStatus().name(),
        document.getAttempt(),
        document.getLastErrorCode(),
        document.getLastErrorMessage(),
        document.getPassageCount(),
        document.getLastResultEventId(),
        document.getCreatedAt(),
        document.getUpdatedAt()
    );

    return new DocumentTransformRequestedMessage(transformRequest, event.getCreatedAt());
  }

  public PassageCreationRequestAvroModel passageCreationRequestEventToPassageCreationRequestAvroModel(
      TransformDocumentEvent event
  ) {
    Document document = event.getDocument();
    return PassageCreationRequestAvroModel.builder()
        .id(UUID.randomUUID().toString())
        .sagaId("")
        .userId(document.getUploader().getValue().toString())
        .collectionId(document.getCollectionId().getValue().toString())
        .metadata(metadataConverter.convertToDatabaseColumn(document.getMetadata()))
        .createdAt(event.getCreatedAt().toInstant(ZoneOffset.UTC))
        .documentStatus(document.getStatus())
        .build();
  }
}
