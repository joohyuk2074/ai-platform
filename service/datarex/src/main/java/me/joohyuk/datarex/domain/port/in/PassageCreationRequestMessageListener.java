package me.joohyuk.datarex.domain.port.in;

import me.joohyuk.datarex.domain.entity.PassageCreationRequestedMessage;

public interface PassageCreationRequestMessageListener {

    void onMessage(PassageCreationRequestedMessage message);
}