package me.joohyuk.datahub.application.port.out.storage;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.Metadata;
import java.io.InputStream;
import me.joohyuk.datahub.application.dto.result.FileStorageResult;

public interface FileStorage {

  FileStorageResult store(
      InputStream inputStream,
      Metadata metadata,
      CollectionId collectionId
  );

  InputStream retrieve(String fileKey);

  void delete(String fileKey);

  boolean exists(String fileKey);
}
