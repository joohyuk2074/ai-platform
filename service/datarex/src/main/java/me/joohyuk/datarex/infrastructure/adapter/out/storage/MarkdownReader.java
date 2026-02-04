package me.joohyuk.datarex.infrastructure.adapter.out.storage;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.joohyuk.datarex.domain.entity.DocumentTransformRequestedMessage.DocumentData;
import me.joohyuk.datarex.domain.exception.DatarexDomainException;
import me.joohyuk.datarex.domain.model.DocumentContent;
import me.joohyuk.datarex.domain.port.out.storage.DocumentReader;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class MarkdownReader implements DocumentReader {

  private static final String STORAGE_BASE_PATH = "storage/documents/";

  @Override
  public List<DocumentContent> read(DocumentData documentData) {
    List<Document> springAiDocuments = loadMarkdown(documentData);
    return toDocumentContents(springAiDocuments);
  }

  private List<Document> loadMarkdown(DocumentData documentData) {
    String filePath = documentData.fileKey();
    if (filePath == null || filePath.isBlank()) {
      throw new DatarexDomainException("fileKey는 필수입니다");
    }

    Map<String, Object> metadata = buildMetadata(documentData);

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

  private Map<String, Object> buildMetadata(DocumentData documentData) {
    Map<String, Object> metadata = new HashMap<>();

    // 문서 기본 정보
    if (documentData.getDocumentId() != null) {
      metadata.put("documentId", documentData.getDocumentId());
    }
    if (documentData.getCollectionId() != null) {
      metadata.put("collectionId", documentData.getCollectionId());
    }

    // 파일 정보
    if (documentData.fileKey() != null) {
      metadata.put("fileKey", documentData.fileKey());
    }
    if (documentData.getFileName() != null) {
      metadata.put("fileName", documentData.getFileName());
    }
    if (documentData.getContentType() != null) {
      metadata.put("contentType", documentData.getContentType());
    }
    if (documentData.getFileSize() != null) {
      metadata.put("fileSize", documentData.getFileSize());
    }

    // 콘텐츠 해시
    if (documentData.getContentHashValue() != null) {
      metadata.put("contentHash", documentData.getContentHashValue());
    }

    // 문서 상태 정보
    if (documentData.status() != null) {
      metadata.put("status", documentData.status());
    }
    metadata.put("attempt", documentData.attempt());

    // 타임스탬프
    if (documentData.createdAt() != null) {
      metadata.put("createdAt", documentData.createdAt().toString());
    }
    if (documentData.updatedAt() != null) {
      metadata.put("updatedAt", documentData.updatedAt().toString());
    }

    return metadata;
  }
}
