package me.joohyuk.datahub.infrastructure.adapter;

import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.PassageCreationRequestEvent;
import me.joohyuk.datahub.infrastructure.adapter.out.persistence.converter.MetadataConverter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PassageMessagingDataMapper {

  private final MetadataConverter metadataConverter;

  public PassageCreationRequestAvroModel passageCreationRequestEventToPassageCreationRequestAvroModel(
      PassageCreationRequestEvent event
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
