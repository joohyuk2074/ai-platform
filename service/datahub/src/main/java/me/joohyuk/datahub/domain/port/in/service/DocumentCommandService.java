package me.joohyuk.datahub.domain.port.in.service;

import com.spartaecommerce.domain.vo.CollectionId;
import java.io.InputStream;
import me.joohyuk.datahub.application.dto.result.TransformDocumentResult;
import me.joohyuk.datahub.application.dto.command.UploadDocumentCommand;
import me.joohyuk.datahub.application.dto.result.UploadDocumentResult;

public interface DocumentCommandService {

  UploadDocumentResult uploadDocument(
      UploadDocumentCommand command,
      InputStream fileInputStream
  );

  TransformDocumentResult requestPassageCreationByCollection(
      CollectionId collectionId
  );
}
