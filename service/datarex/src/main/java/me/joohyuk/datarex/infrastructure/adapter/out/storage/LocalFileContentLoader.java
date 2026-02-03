package me.joohyuk.datarex.infrastructure.adapter.out.storage;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.domain.port.out.storage.FileContentLoader;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalFileContentLoader implements FileContentLoader {

  private final MarkdownReader markdownReader;

    @Override
    public String loadAsText(String storageKey, String contentType) {
        try {
            return Files.readString(Path.of(storageKey), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read file: " + storageKey, e);
        }
    }
}
