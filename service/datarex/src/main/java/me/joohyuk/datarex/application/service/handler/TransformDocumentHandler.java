package me.joohyuk.datarex.application.service.handler;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.util.DateTimeHolder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.application.dto.command.TransformDocumentCommand;
import me.joohyuk.datarex.application.port.out.chunking.DocumentTransformer;
import me.joohyuk.datarex.application.port.out.chunking.DocumentTransformer.ChunkingConfig;
import me.joohyuk.datarex.application.port.out.storage.ChunkedDocumentWriter;
import me.joohyuk.datarex.application.port.out.storage.ChunkedDocumentWriter.StorageConfig;
import me.joohyuk.datarex.application.port.out.storage.DocumentReader;
import me.joohyuk.datarex.domain.exception.DatarexDomainException;
import me.joohyuk.datarex.domain.exception.DatarexErrorCode;
import me.joohyuk.datarex.domain.vo.DocumentContent;
import me.joohyuk.messaging.events.TransformDocumentCompletedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformDocumentHandler {

  private final DocumentReader documentReader;
  private final DocumentTransformer documentTransformer;
  private final ChunkedDocumentWriter chunkedDocumentWriter;
  private final DateTimeHolder dateTimeHolder;

  public TransformDocumentCompletedEvent transform(TransformDocumentCommand command) {
    String eventId = UUID.randomUUID().toString();
    String contentHash = command.contentHash();
    Instant now = dateTimeHolder.now();

    var metadata = command.metadata();

    log.info(
        "문서 변환 요청 수신 - documentId: {}, collectionId: {}, fileKey: {}, fileName: {}, contentType: {}, fileSize: {}",
        command.documentId(),
        command.collectionId(),
        command.fileKey(),
        metadata.fileName(),
        metadata.contentType(),
        metadata.fileSize()
    );

    try {
      List<DocumentContent> documents = documentReader.read(command);

      ChunkingConfig chunkingConfig = ChunkingConfig.defaultConfig();
      List<DocumentContent> splitDocuments = documentTransformer.transform(documents,
          chunkingConfig);

      log.debug("문서 변환(청킹) 완료 - 원본 문서 수: {}, 분할된 청크 수: {}",
          documents.size(), splitDocuments.size());

      StorageConfig storageConfig = new StorageConfig(
          new CollectionId(command.collectionId()),
          new DocumentId(command.documentId()),
          metadata.fileName()
      );
      chunkedDocumentWriter.write(splitDocuments, storageConfig);

      log.info(
          "청크된 문서 저장 완료 - documentId: {}, collectionId: {}, 저장된 청크 수: {}",
          command.documentId(),
          command.collectionId(),
          splitDocuments.size()
      );

      // 성공 이벤트 발행
      return TransformDocumentCompletedEvent.success(
          eventId,
          command.sagaId(),
          String.valueOf(command.collectionId()),
          String.valueOf(command.documentId()),
          contentHash,
          splitDocuments.size(),
          now
      );
    } catch (DatarexDomainException e) {
      log.error(
          "도메인 예외로 문서 변환 실패 - documentId: {}, collectionId: {}, errorCode: {}, message: {}",
          command.documentId(), command.collectionId(), e.getErrorCode().code(), e.getMessage()
      );
      return TransformDocumentCompletedEvent.failure(
          eventId,
          command.sagaId(),
          String.valueOf(command.collectionId()),
          String.valueOf(command.documentId()),
          contentHash,
          e.getErrorCode().code(),
          e.getMessage(),
          now
      );
    } catch (Exception e) {
      log.error(
          "예상치 못한 예외로 문서 변환 실패 - documentId: {}, collectionId: {}",
          command.documentId(), command.collectionId(), e
      );
      return TransformDocumentCompletedEvent.failure(
          eventId,
          command.sagaId(),
          String.valueOf(command.collectionId()),
          String.valueOf(command.documentId()),
          contentHash,
          DatarexErrorCode.UNKNOWN_ERROR.code(),
          e.getMessage(),
          now
      );
    }
  }
}
