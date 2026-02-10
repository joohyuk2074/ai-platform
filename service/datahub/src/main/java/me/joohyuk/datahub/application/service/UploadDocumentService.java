package me.joohyuk.datahub.application.service;

import com.spartaecommerce.domain.vo.ContentHash;
import com.spartaecommerce.domain.vo.Metadata;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.command.UploadDocumentCommand;
import me.joohyuk.datahub.application.dto.result.FileStorageResult;
import me.joohyuk.datahub.application.dto.result.UploadDocumentResult;
import me.joohyuk.datahub.application.port.in.service.UploadDocumentUseCase;
import me.joohyuk.datahub.application.port.out.persistence.DocumentRepository;
import me.joohyuk.datahub.application.port.out.storage.FileStorage;
import me.joohyuk.datahub.application.service.handler.DocumentPersistenceHandler;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadDocumentService implements UploadDocumentUseCase {

  private final FileStorage fileStorage;
  private final DocumentPersistenceHandler persistenceHelper;
  private final DocumentRepository documentRepository;

  @Override
  public UploadDocumentResult uploadDocument(
      UploadDocumentCommand uploadDocumentCommand,
      InputStream fileInputStream
  ) {
    log.info("Starting message upload: {}", uploadDocumentCommand.fileName());

    Metadata metadata = Metadata.forUpload(
        uploadDocumentCommand.fileName(),
        uploadDocumentCommand.fileSize(),
        uploadDocumentCommand.contentType(),
        uploadDocumentCommand.uploadedBy().getValue()
    );

    // 파일 저장 (저장소 어댑터가 SHA-256 해시를 함께 계산하여 반환)
    FileStorageResult result = fileStorage.store(
        fileInputStream,
        metadata,
        uploadDocumentCommand.collectionId()
    );

    String fileKey = result.fileKey();
    ContentHash contentHash = result.contentHash();
    log.info("File stored successfully with key: {}, contentHash: {}", fileKey, contentHash);

    if (documentRepository.existsByContentHash(contentHash)) {
      log.warn("Duplicate file detected. contentHash={}, fileKey={}", contentHash, fileKey);
      compensateFileUpload(fileKey);
      throw new DatahubDomainException(
          "Duplicate file detected. contentHash: " + contentHash,
          DatahubErrorCode.DOCUMENT_ALREADY_EXISTS
      );
    }

    Document document = Document.create(
        uploadDocumentCommand.collectionId(), fileKey, contentHash, metadata);

    try {
      DocumentUploadedEvent documentUploadedEvent = persistenceHelper.persistDocument(document);
      return UploadDocumentResult.from(documentUploadedEvent.getDocument());
    } catch (Exception e) {
      // DB 저장 실패 시 보상 트랜잭션: 이미 업로드된 파일 삭제
      log.error("DB save failed, initiating compensating transaction to delete uploaded file: {}",
          fileKey, e);
      compensateFileUpload(fileKey);
      throw new DatahubDomainException(
          "Failed to save document to database",
          DatahubErrorCode.DOCUMENT_PROCESSING_FAILED,
          e
      );
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
