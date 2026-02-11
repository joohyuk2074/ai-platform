package me.joohyuk.datarex.application.port.in;

import me.joohyuk.messaging.events.DocumentTransformRequestedMessage;

public interface DocumentTransformRequestMessageListener {

    void onMessage(DocumentTransformRequestedMessage message);
}