package me.joohyuk.datarex.domain.port.out.storage;

import java.util.List;
import me.joohyuk.datarex.domain.model.DocumentContent;

public interface ChunkedDocumentWriter {

  void write(List<DocumentContent> chunks, StorageConfig config);

  record StorageConfig(
      Long collectionId,
      Long documentId,
      String fileName
  ) {

  }
}
