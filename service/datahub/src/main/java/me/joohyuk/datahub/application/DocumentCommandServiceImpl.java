package me.joohyuk.datahub.application;

import com.spartaecommerce.domain.vo.DocumentId;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.request.InitiateChunkedUploadCommand;
import me.joohyuk.datahub.application.dto.request.UploadChunkCommand;
import me.joohyuk.datahub.application.dto.request.UploadDocumentCommand;
import me.joohyuk.datahub.application.dto.response.InitiateChunkedUploadResult;
import me.joohyuk.datahub.application.dto.response.UploadChunkResult;
import me.joohyuk.datahub.application.dto.response.UploadDocumentResult;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.exception.IngestionDomainException;
import me.joohyuk.datahub.domain.port.in.service.DocumentCommandService;
import me.joohyuk.datahub.domain.port.out.storage.ChunkUploadStore;
import me.joohyuk.datahub.domain.vo.ChunkUploadSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentCommandServiceImpl implements DocumentCommandService {

  private final DocumentCreateCommandHandler documentCreateCommandHandler;
  private final ChunkUploadStore chunkUploadStore;

  /** 청크 크기 (바이트). 기본값 5 MB. application.yml에서 datahub.chunked-upload.chunk-size로 조정 가능 */
  @Value("${datahub.chunked-upload.chunk-size:5242880}")
  private int chunkSize;

  // ─── 단일 업로드 ────────────────────────────────────────────────

  @Override
  public UploadDocumentResult uploadDocument(
      UploadDocumentCommand command,
      InputStream fileInputStream
  ) {
    DocumentUploadedEvent uploadedDocumentEvent =
        documentCreateCommandHandler.uploadDocument(command, fileInputStream);

    return UploadDocumentResult.from(uploadedDocumentEvent.getDocument());
  }

  // ─── 멀티파트 청크 업로드 ───────────────────────────────────────

  @Override
  public InitiateChunkedUploadResult initiateChunkedUpload(InitiateChunkedUploadCommand command) {
    String uploadId = UUID.randomUUID().toString();
    int totalChunks = (int) Math.ceil((double) command.totalSize() / chunkSize);

    ChunkUploadSession session = new ChunkUploadSession(
        uploadId,
        command.collectionId(),
        command.fileName(),
        command.totalSize(),
        command.contentType(),
        command.uploadedBy(),
        totalChunks,
        chunkSize
    );

    chunkUploadStore.createSession(session);

    log.info("Chunked upload session created: uploadId={}, totalChunks={}, chunkSize={}",
        uploadId, totalChunks, chunkSize);

    return new InitiateChunkedUploadResult(uploadId, chunkSize, totalChunks, command.totalSize());
  }

  @Override
  public UploadChunkResult uploadChunk(UploadChunkCommand command, InputStream chunkInputStream) {
    ChunkUploadSession session = chunkUploadStore.findSession(command.uploadId())
        .orElseThrow(() ->
            new IngestionDomainException("Upload session not found: " + command.uploadId()));

    if (!session.isValidChunkIndex(command.chunkIndex())) {
      throw new IngestionDomainException(
          String.format("Invalid chunk index %d. Valid range: 0 to %d",
              command.chunkIndex(), session.getTotalChunks() - 1));
    }

    // 중복 청크는 멱등적으로 처리 (재시도 시 안전)
    if (!session.markChunkReceived(command.chunkIndex())) {
      log.warn("Duplicate chunk upload detected: uploadId={}, chunkIndex={}",
          command.uploadId(), command.chunkIndex());
    }

    chunkUploadStore.storeChunk(command.uploadId(), command.chunkIndex(), chunkInputStream);

    log.info("Chunk uploaded: uploadId={}, chunkIndex={}, received={}/{}",
        command.uploadId(), command.chunkIndex(),
        session.getReceivedChunkCount(), session.getTotalChunks());

    return new UploadChunkResult(
        command.uploadId(),
        command.chunkIndex(),
        session.getReceivedChunkCount(),
        session.getTotalChunks(),
        session.isAllChunksReceived()
    );
  }

  @Override
  public UploadDocumentResult completeChunkedUpload(String uploadId) {
    ChunkUploadSession session = chunkUploadStore.findSession(uploadId)
        .orElseThrow(() ->
            new IngestionDomainException("Upload session not found: " + uploadId));

    if (!session.isAllChunksReceived()) {
      throw new IngestionDomainException(
          String.format("Not all chunks received. Received: %d/%d",
              session.getReceivedChunkCount(), session.getTotalChunks()));
    }

    log.info("All chunks received, assembling file: uploadId={}", uploadId);

    InputStream assembledInputStream = chunkUploadStore.assembleChunks(uploadId);

    // 조합된 스트림으로 단일 업로드와 동일한 커맨드를 생성하여 기존 로직 재사용
    UploadDocumentCommand command = new UploadDocumentCommand(
        session.getCollectionId(),
        session.getFileName(),
        session.getTotalSize(),
        session.getContentType(),
        session.getUploadedBy()
    );

    try {
      DocumentUploadedEvent event =
          documentCreateCommandHandler.uploadDocument(command, assembledInputStream);

      log.info("Chunked upload completed successfully: uploadId={}, documentId={}",
          uploadId, event.getDocument().getId().getValue());

      return UploadDocumentResult.from(event.getDocument());
    } finally {
      // 성공/실패 상관없이 임시 세션 정리
      chunkUploadStore.deleteSession(uploadId);
    }
  }

  // ─── 문서 삭제 ──────────────────────────────────────────────────

  /**
   * 문서를 삭제합니다.
   *
   * @param documentId 삭제할 문서 ID
   */
  @Transactional
  public void deleteDocument(DocumentId documentId) {
    // TODO: 구현
    // 1. Document 조회
    // 2. 파일 저장소에서 파일 삭제
    // 3. DB에서 Document 삭제
  }
}
