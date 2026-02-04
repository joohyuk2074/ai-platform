package me.joohyuk.datarex.infrastructure.adapter.out.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.domain.exception.DatarexDomainException;
import me.joohyuk.datarex.domain.model.DocumentContent;
import me.joohyuk.datarex.domain.port.out.storage.ChunkedDocumentWriter;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FileSystemChunkedDocumentWriter implements ChunkedDocumentWriter {

  private static final String BASE_STORAGE_PATH = "storage/documents/chunks";

  @Override
  public void write(List<DocumentContent> chunks, StorageConfig config) {
    String outputPath = buildOutputPath(config);

    log.debug(
        "청크된 문서 저장 시작 - collectionId: {}, documentId: {}, fileName: {}, 청크 수: {}",
        config.collectionId(),
        config.documentId(),
        config.fileName(),
        chunks.size()
    );

    createDirectoryIfNotExists(outputPath);
    writeChunksToFile(chunks, outputPath);

    log.info(
        "청크된 문서 저장 완료 - collectionId: {}, documentId: {}, 저장 경로: {}, 청크 수: {}",
        config.collectionId(),
        config.documentId(),
        outputPath,
        chunks.size()
    );
  }

  private String buildOutputPath(StorageConfig config) {
    return String.format("%s/%d/%d/%s",
        BASE_STORAGE_PATH,
        config.collectionId(),
        config.documentId(),
        config.fileName()
    );
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
      throw new DatarexDomainException("디렉토리 생성 실패: " + outputPath, e);
    }
  }

  private void writeChunksToFile(List<DocumentContent> chunks, String outputPath) {
    List<Document> springAiDocuments = toSpringAiDocuments(chunks);

    try {
      FileDocumentWriter writer = new FileDocumentWriter(
          outputPath,
          true,              // append mode
          MetadataMode.ALL,  // 모든 메타데이터 포함
          false              // overwrite = false
      );
      writer.accept(springAiDocuments);
      log.debug("청크 파일 쓰기 완료: {}", outputPath);
    } catch (Exception e) {
      log.error("청크 파일 쓰기 실패: {}", outputPath, e);
      throw new DatarexDomainException("청크된 문서 파일 저장 실패: " + outputPath, e);
    }
  }

  private List<Document> toSpringAiDocuments(List<DocumentContent> documents) {
    return documents.stream()
        .map(doc -> new Document(doc.content(), doc.metadata()))
        .collect(Collectors.toList());
  }
}