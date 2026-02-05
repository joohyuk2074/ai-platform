package me.joohyuk.datarex.domain.port.out.storage;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.DocumentId;
import java.util.List;
import me.joohyuk.datarex.domain.vo.DocumentContent;

public interface ChunkedDocumentWriter {

  void write(List<DocumentContent> chunks, StorageConfig config);

  record StorageConfig(
      CollectionId collectionId,
      DocumentId documentId,
      String fileName
  ) {

  }
}
