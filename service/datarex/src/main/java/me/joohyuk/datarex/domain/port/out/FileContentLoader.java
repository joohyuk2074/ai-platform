package me.joohyuk.datarex.domain.port.out;

public interface FileContentLoader {

    String loadAsText(String storageKey, String contentType);
}
