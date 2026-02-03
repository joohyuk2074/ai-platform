package me.joohyuk.datarex.domain.port.in.listener;

import me.joohyuk.datarex.domain.entity.PassageCreationRequestedMessage;

public interface PassageCreationRequestMessageListener {

    void onMessage(PassageCreationRequestedMessage message);
}