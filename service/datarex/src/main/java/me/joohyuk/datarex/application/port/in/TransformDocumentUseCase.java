package me.joohyuk.datarex.application.port.in;

import me.joohyuk.datarex.application.dto.command.TransformDocumentCommand;

public interface TransformDocumentUseCase {

  void transformDocument(TransformDocumentCommand transformDocumentCommand);
}
