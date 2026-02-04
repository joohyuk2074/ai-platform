package me.joohyuk.datarex.domain.port.in.listener;

import me.joohyuk.datarex.domain.entity.DocumentTransformRequestedMessage;

public interface DocumentTransformRequestMessageListener {

    void onMessage(DocumentTransformRequestedMessage message);
}