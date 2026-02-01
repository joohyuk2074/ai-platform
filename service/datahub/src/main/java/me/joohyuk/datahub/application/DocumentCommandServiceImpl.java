package me.joohyuk.datahub.application;

import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.request.UploadDocumentCommand;
import me.joohyuk.datahub.application.dto.response.UploadDocumentResult;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.port.in.service.DocumentCommandService;
import me.joohyuk.datahub.domain.vo.CollectionId;
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
  public int requestPassageCreationByCollection(CollectionId collectionId) {
    return passageCreationRequestCommandHandler.requestPassageCreationByCollection(collectionId);
  }
}
