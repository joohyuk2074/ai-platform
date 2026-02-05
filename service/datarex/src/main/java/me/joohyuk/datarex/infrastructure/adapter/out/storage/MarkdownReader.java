package me.joohyuk.datarex.infrastructure.adapter.out.storage;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.joohyuk.datarex.domain.exception.DatarexDomainException;
import me.joohyuk.datarex.domain.port.out.storage.DocumentReader;
import me.joohyuk.datarex.domain.vo.DocumentContent;
import me.joohyuk.messaging.events.DocumentTransformRequestedMessage.DocumentTransformRequest;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class MarkdownReader implements DocumentReader {

  private static final String STORAGE_BASE_PATH = "storage/documents/";

  @Override
  public List<DocumentContent> read(DocumentTransformRequest documentTransformRequest) {
    List<Document> springAiDocuments = loadMarkdown(documentTransformRequest);
    return toDocumentContents(springAiDocuments);
  }

  private List<Document> loadMarkdown(DocumentTransformRequest documentTransformRequest) {
    String filePath = documentTransformRequest.fileKey();
    if (filePath == null || filePath.isBlank()) {
      throw new DatarexDomainException("fileKey는 필수입니다");
    }

    Map<String, Object> metadata = buildMetadata(documentTransformRequest);

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

  private Map<String, Object> buildMetadata(DocumentTransformRequest documentTransformRequest) {
    Map<String, Object> metadata = new HashMap<>();

    // 문서 기본 정보
    if (documentTransformRequest.documentId() != null) {
      metadata.put("documentId", documentTransformRequest.documentId());
    }
    if (documentTransformRequest.collectionId() != null) {
      metadata.put("collectionId", documentTransformRequest.collectionId());
    }

    // 파일 정보
    if (documentTransformRequest.fileKey() != null) {
      metadata.put("fileKey", documentTransformRequest.fileKey());
    }
    if (documentTransformRequest.metadata().fileName() != null) {
      metadata.put("fileName", documentTransformRequest.metadata().fileName());
    }
    if (documentTransformRequest.metadata().contentType() != null) {
      metadata.put("contentType", documentTransformRequest.metadata().contentType());
    }
    if (documentTransformRequest.metadata().fileSize() != null) {
      metadata.put("fileSize", documentTransformRequest.metadata().fileSize());
    }

    // 콘텐츠 해시
    if (documentTransformRequest.contentHash() != null) {
      metadata.put("contentHash", documentTransformRequest.contentHash());
    }

    // 문서 상태 정보
    if (documentTransformRequest.status() != null) {
      metadata.put("status", documentTransformRequest.status());
    }
    metadata.put("attempt", documentTransformRequest.attempt());

    // 타임스탬프
    if (documentTransformRequest.createdAt() != null) {
      metadata.put("createdAt", documentTransformRequest.createdAt().toString());
    }
    if (documentTransformRequest.updatedAt() != null) {
      metadata.put("updatedAt", documentTransformRequest.updatedAt().toString());
    }

    return metadata;
  }
}
