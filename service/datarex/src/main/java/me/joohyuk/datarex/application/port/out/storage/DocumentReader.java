package me.joohyuk.datarex.application.port.out.storage;

import java.util.List;
import me.joohyuk.messaging.events.DocumentTransformRequestedMessage.DocumentTransformRequest;
import me.joohyuk.datarex.domain.vo.DocumentContent;

public interface DocumentReader {

    List<DocumentContent> read(DocumentTransformRequest documentTransformRequest);
}