package me.joohyuk.datahub.application;

import com.spartaecommerce.domain.vo.CollectionId;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.result.TransformDocumentResult;
import me.joohyuk.datahub.application.dto.command.UploadDocumentCommand;
import me.joohyuk.datahub.application.dto.result.UploadDocumentResult;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.port.in.service.DocumentCommandService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentCommandServiceImpl implements DocumentCommandService {

  private final DocumentUploadCommandHandler documentUploadCommandHandler;
  private final PassageCreationRequestCommandHandler passageCreationRequestCommandHandler;

  @Override
  public UploadDocumentResult uploadDocument(
      UploadDocumentCommand command,
      InputStream fileInputStream
  ) {
    DocumentUploadedEvent uploadedDocumentEvent =
        documentUploadCommandHandler.uploadDocument(command, fileInputStream);

    return UploadDocumentResult.from(uploadedDocumentEvent.getDocument());
  }

  @Override
  public TransformDocumentResult requestPassageCreationByCollection(
      CollectionId collectionId
  ) {
    return passageCreationRequestCommandHandler.requestPassageCreationByCollection(collectionId);
  }
}
