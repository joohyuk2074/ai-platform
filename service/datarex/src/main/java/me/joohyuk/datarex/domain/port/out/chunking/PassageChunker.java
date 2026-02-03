package me.joohyuk.datarex.domain.port.out.chunking;

import java.util.List;

public interface PassageChunker {

    List<String> chunk(String text, String contentType, String passageVersion);
}
