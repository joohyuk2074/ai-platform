package me.joohyuk.datarex.domain.port.out;

import java.util.List;

public interface PassageChunker {

    List<String> chunk(String text, String contentType, String passageVersion);
}
