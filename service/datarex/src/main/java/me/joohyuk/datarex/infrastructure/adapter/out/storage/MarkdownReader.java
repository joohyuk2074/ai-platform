package me.joohyuk.datarex.infrastructure.adapter.out.storage;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.joohyuk.datarex.domain.exception.DatarexDomainException;
import me.joohyuk.datarex.application.port.out.storage.DocumentReader;
import me.joohyuk.datarex.domain.vo.DocumentContent;
import me.joohyuk.messaging.events.DocumentTransformRequestedMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class MarkdownReader implements DocumentReader {

  private static final String STORAGE_BASE_PATH = "storage/documents/";

  @Override
  public List<DocumentContent> read(DocumentTransformRequestedMessage.Document document) {
    List<Document> springAiDocuments = loadMarkdown(document);
    return toDocumentContents(springAiDocuments);
  }

  private List<Document> loadMarkdown(DocumentTransformRequestedMessage.Document document) {
    String filePath = document.fileKey();
    if (filePath == null || filePath.isBlank()) {
      throw new DatarexDomainException("fileKey는 필수입니다");
    }

    Map<String, Object> metadata = buildMetadata(document);

    MarkdownDocumentReaderConfig.Builder builder = MarkdownDocumentReaderConfig.builder()
        .withHorizontalRuleCreateDocument(true)
        .withIncludeCodeBlock(false)
        .withIncludeBlockquote(false);

    metadata.forEach(builder::withAdditionalMetadata);

    MarkdownDocumentReaderConfig config = builder.build();

    FileSystemResource resource = new FileSystemResource(Path.of(STORAGE_BASE_PATH + filePath));
    if (!resource.exists()) {
      throw new IllegalStateException("파일이 존재하지 않습니다: " + filePath);
    }

    MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
    return reader.get();
  }

  private List<DocumentContent> toDocumentContents(List<Document> documents) {
    return documents.stream()
        .map(doc -> new DocumentContent(doc.getText(), doc.getMetadata()))
        .collect(Collectors.toList());
  }

  private Map<String, Object> buildMetadata(DocumentTransformRequestedMessage.Document document) {
    Map<String, Object> metadata = new HashMap<>();

    // 문서 기본 정보
    if (document.documentId() != null) {
      metadata.put("documentId", document.documentId());
    }
    if (document.collectionId() != null) {
      metadata.put("collectionId", document.collectionId());
    }

    // 파일 정보
    if (document.fileKey() != null) {
      metadata.put("fileKey", document.fileKey());
    }
    if (document.metadata().fileName() != null) {
      metadata.put("fileName", document.metadata().fileName());
    }
    if (document.metadata().contentType() != null) {
      metadata.put("contentType", document.metadata().contentType());
    }
    if (document.metadata().fileSize() != null) {
      metadata.put("fileSize", document.metadata().fileSize());
    }

    // 콘텐츠 해시
    if (document.contentHash() != null) {
      metadata.put("contentHash", document.contentHash());
    }

    // 문서 상태 정보
    if (document.status() != null) {
      metadata.put("status", document.status());
    }
    metadata.put("attempt", document.attempt());

    // 타임스탬프
    if (document.createdAt() != null) {
      metadata.put("createdAt", document.createdAt().toString());
    }
    if (document.updatedAt() != null) {
      metadata.put("updatedAt", document.updatedAt().toString());
    }

    return metadata;
  }
}
