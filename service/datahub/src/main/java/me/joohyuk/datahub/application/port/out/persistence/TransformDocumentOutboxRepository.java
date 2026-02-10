package me.joohyuk.datahub.application.port.out.persistence;

import java.util.List;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;

public interface TransformDocumentOutboxRepository {

  TransformDocumentOutbox save(TransformDocumentOutbox transformDocumentOutbox);

  List<TransformDocumentOutbox> saveAll(List<TransformDocumentOutbox> transformDocumentOutboxes);
}
