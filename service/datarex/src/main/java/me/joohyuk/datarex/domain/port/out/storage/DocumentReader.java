package me.joohyuk.datarex.domain.port.out.storage;

import java.util.List;
import me.joohyuk.datarex.domain.entity.DocumentTransformRequestedMessage.DocumentData;
import me.joohyuk.datarex.domain.model.DocumentContent;

public interface DocumentReader {

    List<DocumentContent> read(DocumentData documentData);
}