package me.joohyuk.datarex.application.port.out.storage;

public interface FileContentLoader {

    String loadAsText(String storageKey, String contentType);
}
