package me.joohyuk.datarex.infrastructure.adapter.out.storage;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.joohyuk.datarex.domain.entity.PassageCreationRequestedMessage.DocumentData;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class MarkdownReader {

  public List<Document> loadMarkdown(DocumentData documentData) {
    String filePath = documentData.fileKey();
    if (filePath == null || filePath.isBlank()) {
      throw new IllegalArgumentException("fileKey는 필수입니다");
    }

    Map<String, Object> metadata = buildMetadata(documentData);

    // MarkdownDocumentReaderConfig 생성
    MarkdownDocumentReaderConfig.Builder configBuilder = MarkdownDocumentReaderConfig.builder()
        .withHorizontalRuleCreateDocument(true)
        .withIncludeCodeBlock(false)
        .withIncludeBlockquote(false);

    // 메타정보를 config에 추가
    metadata.forEach(configBuilder::withAdditionalMetadata);

    MarkdownDocumentReaderConfig config = configBuilder.build();

    // 파일 시스템 리소스로 문서 읽기
    FileSystemResource resource = new FileSystemResource(Path.of("storage/documents/" + filePath));
    if (!resource.exists()) {
      throw new IllegalStateException("파일이 존재하지 않습니다: " + filePath);
    }

    MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
    return reader.get();
  }

  /**
   * DocumentData로부터 메타정보를 추출하여 Map으로 변환합니다.
   */
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
