package me.joohyuk.datahub.application;

import com.spartaecommerce.domain.vo.Metadata;
import com.spartaecommerce.exception.ErrorCode;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.request.UploadDocumentCommand;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.exception.IngestionDomainException;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentRepository;
import me.joohyuk.datahub.domain.port.out.storage.FileStorage;
import com.spartaecommerce.domain.vo.ContentHash;
import me.joohyuk.datahub.infrastructure.util.ContentHasher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentUploadCommandHandler {

  private final FileStorage fileStorage;
  private final DocumentPersistenceHelper persistenceHelper;
  private final DocumentRepository documentRepository;

  public DocumentUploadedEvent uploadDocument(
      UploadDocumentCommand uploadDocumentCommand,
      InputStream fileInputStream
  ) {
    log.info("Starting message upload: {}", uploadDocumentCommand.fileName());

    Metadata metadata = Metadata.of(
        uploadDocumentCommand.fileName(),
        uploadDocumentCommand.fileSize(),
        uploadDocumentCommand.contentType(),
        uploadDocumentCommand.uploadedBy().getValue()
    );

    // Phase 1: 스트림을 감싸서 저장소에 전달 (저장 중 SHA-256 해시를 동시에 계산)
    var hashingStream = ContentHasher.wrap(fileInputStream);

    String scope = "collections/" + uploadDocumentCommand.collectionId().getValue();
    String fileKey = fileStorage.store(hashingStream, metadata, scope);

    // Phase 2: 스트림 소비 후 해시 추출
    ContentHash contentHash = hashingStream.getContentHash();
    log.info("File stored successfully with key: {}, contentHash: {}", fileKey, contentHash);

    if (documentRepository.existsByContentHash(contentHash)) {
      log.warn("Duplicate file detected. contentHash={}, fileKey={}", contentHash, fileKey);
      compensateFileUpload(fileKey);
      throw new IngestionDomainException("Duplicate file detected. contentHash: " + contentHash);
    }

    Document document = Document.create(
        uploadDocumentCommand.collectionId(), fileKey, contentHash, metadata);

    // DB 저장 (트랜잭션 안에서 수행) - Helper에 위임
    try {
      return persistenceHelper.persistDocument(document);
    } catch (Exception e) {
      // DB 저장 실패 시 보상 트랜잭션: 이미 업로드된 파일 삭제
      log.error("DB save failed, initiating compensating transaction to delete uploaded file: {}",
          fileKey, e);
      compensateFileUpload(fileKey);
      throw new IngestionDomainException(ErrorCode.INVALID_REQUEST.getMessage(), e);
    }
  }

  private void compensateFileUpload(String fileKey) {
    try {
      fileStorage.delete(fileKey);
      log.info("Compensating transaction completed: deleted file with key: {}", fileKey);
    } catch (Exception deleteException) {
      // 파일 삭제 실패는 로그만 남기고 원본 예외를 유지
      // 운영팀이 수동으로 정리하거나 별도의 정리 작업으로 처리
      log.error(
          "Failed to delete file during compensation. Manual cleanup may be required for file: {}",
          fileKey, deleteException);
    }
  }
}
