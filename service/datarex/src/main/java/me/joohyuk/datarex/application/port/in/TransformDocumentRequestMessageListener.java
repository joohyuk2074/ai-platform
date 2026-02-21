package me.joohyuk.datarex.application.port.in;

import java.util.List;
import me.joohyuk.datarex.application.dto.command.TransformDocumentCommand;
import org.springframework.kafka.support.Acknowledgment;

public interface TransformDocumentRequestMessageListener {

  void onMessage(List<TransformDocumentCommand> messages, Acknowledgment ack);
}