package me.joohyuk.datahub.application.port.in.service;

import java.io.InputStream;
import me.joohyuk.datahub.application.dto.command.UploadDocumentCommand;
import me.joohyuk.datahub.application.dto.result.UploadDocumentResult;

public interface UploadDocumentUseCase {

  UploadDocumentResult uploadDocument(UploadDocumentCommand command, InputStream fileInputStream);
}
