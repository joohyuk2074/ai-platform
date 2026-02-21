package me.joohyuk.datarex.application.port.out.storage;

import java.util.List;
import me.joohyuk.datarex.application.dto.command.TransformDocumentCommand;
import me.joohyuk.datarex.domain.vo.DocumentContent;

public interface DocumentReader {

  List<DocumentContent> read(TransformDocumentCommand transformDocumentCommand);
}