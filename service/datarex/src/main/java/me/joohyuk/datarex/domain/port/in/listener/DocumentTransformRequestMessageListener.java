package me.joohyuk.datarex.domain.port.in.listener;

import me.joohyuk.messaging.events.DocumentTransformRequestedMessage;

public interface DocumentTransformRequestMessageListener {

    void onMessage(DocumentTransformRequestedMessage message);
}