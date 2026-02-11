package me.joohyuk.datarex.application.service;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.DocumentId;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.domain.exception.DatarexDomainException;
import me.joohyuk.datarex.application.port.in.DocumentTransformUseCase;
import me.joohyuk.datarex.application.port.out.chunking.DocumentTransformer;
import me.joohyuk.datarex.application.port.out.chunking.DocumentTransformer.ChunkingConfig;
import me.joohyuk.datarex.application.port.out.message.DocumentTransformResultEventPublisher;
import me.joohyuk.datarex.application.port.out.storage.ChunkedDocumentWriter;
import me.joohyuk.datarex.application.port.out.storage.ChunkedDocumentWriter.StorageConfig;
import me.joohyuk.datarex.application.port.out.storage.DocumentReader;
import me.joohyuk.datarex.domain.vo.DocumentContent;
import me.joohyuk.messaging.events.DocumentTransformCompletedMessage;
import me.joohyuk.messaging.events.DocumentTransformFailedMessage;
import me.joohyuk.messaging.events.DocumentTransformRequestedMessage;
import me.joohyuk.messaging.events.DocumentTransformRequestedMessage.DocumentTransformRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentTransformService implements DocumentTransformUseCase {

  private final DocumentReader documentReader;
  private final DocumentTransformer documentTransformer;
  private final ChunkedDocumentWriter chunkedDocumentWriter;
  private final DocumentTransformResultEventPublisher eventPublisher;

  @Override
  public void transformDocument(DocumentTransformRequestedMessage message) {
    DocumentTransformRequest document = message.message();
    String eventId = UUID.randomUUID().toString();
    String passageVersion = document.contentHash();

    log.info(
        "문서 변환 요청 수신 - documentId: {}, collectionId: {}, fileKey: {}, fileName: {}, contentType: {}, fileSize: {}",
        document.documentId(),
        document.collectionId(),
        document.fileKey(),
        document.metadata().fileName(),
        document.metadata().contentType(),
        document.metadata().fileSize()
    );

    try {
      List<DocumentContent> documents = documentReader.read(document);

      ChunkingConfig chunkingConfig = ChunkingConfig.defaultConfig();
      List<DocumentContent> splitDocuments = documentTransformer.transform(documents,
          chunkingConfig);

      log.debug("문서 변환(청킹) 완료 - 원본 문서 수: {}, 분할된 청크 수: {}", documents.size(),
          splitDocuments.size());

      StorageConfig storageConfig = new StorageConfig(
          new CollectionId(document.collectionId()),
          new DocumentId(document.documentId()),
          document.metadata().fileName()
      );
      chunkedDocumentWriter.write(splitDocuments, storageConfig);

      log.info(
          "청크된 문서 저장 완료 - documentId: {}, collectionId: {}, 저장된 청크 수: {}",
          document.documentId(),
          document.collectionId(),
          splitDocuments.size()
      );

      // 성공 이벤트 발행
      DocumentTransformCompletedMessage completedMessage = new DocumentTransformCompletedMessage(
          eventId,
          String.valueOf(document.collectionId()),
          String.valueOf(document.documentId()),
          passageVersion,
          splitDocuments.size(),
          Instant.now()
      );
      eventPublisher.publishCompleted(completedMessage);

      log.info(
          "문서 변환 완료 이벤트 발행 - eventId: {}, documentId: {}, passageCount: {}",
          eventId,
          document.documentId(),
          splitDocuments.size()
      );

    } catch (Exception e) {
      log.error(
          "문서 변환 실패 - documentId: {}, collectionId: {}, error: {}",
          document.documentId(),
          document.collectionId(),
          e.getMessage(),
          e
      );

      // 실패 이벤트 발행
      DocumentTransformFailedMessage failedMessage = new DocumentTransformFailedMessage(
          eventId,
          String.valueOf(document.collectionId()),
          String.valueOf(document.documentId()),
          passageVersion,
          determineErrorCode(e),
          e.getMessage(),
          isRetryable(e),
          Instant.now()
      );
      eventPublisher.publishFailed(failedMessage);

      log.info(
          "문서 변환 실패 이벤트 발행 - eventId: {}, documentId: {}, errorCode: {}, retryable: {}",
          eventId,
          document.documentId(),
          determineErrorCode(e),
          isRetryable(e)
      );

      throw new DatarexDomainException("문서 변환 처리 중 오류 발생", e);
    }
  }

  private String determineErrorCode(Exception e) {
    // 예외 타입에 따라 에러 코드 결정
    if (e instanceof IllegalArgumentException) {
      return "INVALID_DOCUMENT";
    } else if (e instanceof IOException) {
      return "STORAGE_ERROR";
    }
    return "UNKNOWN_ERROR";
  }

  private boolean isRetryable(Exception e) {
    return e instanceof IOException;
  }
}
