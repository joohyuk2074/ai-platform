package me.joohyuk.datarex.domain.port.out.storage;

public interface FileContentLoader {

    String loadAsText(String storageKey, String contentType);
}
