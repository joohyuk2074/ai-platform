package me.joohyuk.datarex.infrastructure.adapter.out.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.domain.exception.DatarexDomainException;
import me.joohyuk.datarex.application.port.out.storage.ChunkedDocumentWriter;
import me.joohyuk.datarex.domain.exception.DatarexErrorCode;
import me.joohyuk.datarex.domain.vo.DocumentContent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileSystemChunkedDocumentWriter implements ChunkedDocumentWriter {

  private static final String BASE_STORAGE_PATH = "storage/documents/chunks";
  private final ObjectMapper objectMapper;

  @Override
  public void write(List<DocumentContent> chunks, StorageConfig config) {
    String outputPath = buildOutputPath(config);

    log.debug(
        "청크된 문서 저장 시작 - collectionId: {}, documentId: {}, fileName: {}, 청크 수: {}",
        config.collectionId().getValue(),
        config.documentId().getValue(),
        config.fileName(),
        chunks.size()
    );

    createDirectoryIfNotExists(outputPath);
    writeChunksToFile(chunks, outputPath);

    log.info(
        "청크된 문서 저장 완료 - collectionId: {}, documentId: {}, 저장 경로: {}, 청크 수: {}",
        config.collectionId().getValue(),
        config.documentId().getValue(),
        outputPath,
        chunks.size()
    );
  }

  private String buildOutputPath(StorageConfig config) {
    // 파일 확장자를 .jsonl로 변경
    String fileNameWithoutExt = removeExtension(config.fileName());
    return String.format("%s/%d/%d/%s.jsonl",
        BASE_STORAGE_PATH,
        config.collectionId().getValue(),
        config.documentId().getValue(),
        fileNameWithoutExt
    );
  }

  private String removeExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf('.');
    return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
  }

  private void createDirectoryIfNotExists(String outputPath) {
    try {
      Path directoryPath = Paths.get(outputPath).getParent();
      if (directoryPath != null) {
        Files.createDirectories(directoryPath);
        log.debug("디렉토리 생성 또는 확인 완료: {}", directoryPath);
      }
    } catch (IOException e) {
      log.error("디렉토리 생성 실패: {}", outputPath, e);
      throw new DatarexDomainException(
          "디렉토리 생성 실패: " + outputPath,
          DatarexErrorCode.FILE_STORAGE_FAILED,
          e);
    }
  }

  private void writeChunksToFile(List<DocumentContent> chunks, String outputPath) {
    try (BufferedWriter writer = Files.newBufferedWriter(
        Paths.get(outputPath),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
    )) {
      for (DocumentContent chunk : chunks) {
        // 각 청크를 JSON으로 직렬화하여 한 줄씩 저장 (JSONL 형식)
        String json = objectMapper.writeValueAsString(chunk);
        writer.write(json);
        writer.newLine();
      }
      log.debug("청크 파일 쓰기 완료: {} ({} chunks)", outputPath, chunks.size());
    } catch (IOException e) {
      log.error("청크 파일 쓰기 실패: {}", outputPath, e);
      throw new DatarexDomainException("청크된 문서 파일 저장 실패: " + outputPath, DatarexErrorCode.FILE_STORAGE_FAILED, e);
    }
  }
}