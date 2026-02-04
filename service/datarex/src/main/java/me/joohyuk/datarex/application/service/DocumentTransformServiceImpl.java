package me.joohyuk.datarex.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.domain.entity.DocumentTransformRequestedMessage;
import me.joohyuk.datarex.domain.entity.DocumentTransformRequestedMessage.DocumentData;
import me.joohyuk.datarex.domain.model.DocumentContent;
import me.joohyuk.datarex.domain.port.in.service.DocumentTransformService;
import me.joohyuk.datarex.domain.port.out.chunking.DocumentTransformer;
import me.joohyuk.datarex.domain.port.out.chunking.DocumentTransformer.ChunkingConfig;
import me.joohyuk.datarex.domain.port.out.storage.ChunkedDocumentWriter;
import me.joohyuk.datarex.domain.port.out.storage.ChunkedDocumentWriter.StorageConfig;
import me.joohyuk.datarex.domain.port.out.storage.DocumentReader;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentTransformServiceImpl implements DocumentTransformService {

  private final DocumentReader documentReader;
  private final DocumentTransformer documentTransformer;
  private final ChunkedDocumentWriter chunkedDocumentWriter;

  public void transformDocument(DocumentTransformRequestedMessage message) {
    DocumentData document = message.document();

    log.info(
        "문서 변환 요청 수신 - documentId: {}, collectionId: {}, fileKey: {}, fileName: {}, contentType: {}, fileSize: {}",
        document.getDocumentId(),
        document.getCollectionId(),
        document.fileKey(),
        document.getFileName(),
        document.getContentType(),
        document.getFileSize()
    );

    List<DocumentContent> documents = documentReader.read(document);

    ChunkingConfig chunkingConfig = ChunkingConfig.defaultConfig();
    List<DocumentContent> splitDocuments = documentTransformer.transform(documents, chunkingConfig);

    log.debug("문서 변환(청킹) 완료 - 원본 문서 수: {}, 분할된 청크 수: {}", documents.size(), splitDocuments.size());

    StorageConfig storageConfig = new StorageConfig(
        document.getCollectionId(),
        document.getDocumentId(),
        document.getFileName()
    );
    chunkedDocumentWriter.write(splitDocuments, storageConfig);

    log.info(
        "청크된 문서 저장 완료 - documentId: {}, collectionId: {}, 저장된 청크 수: {}",
        document.getDocumentId(),
        document.getCollectionId(),
        splitDocuments.size()
    );

    // 4. 결과 이벤트 발행 (DocumentTransformResultEventPublisher)
    // TODO: 이벤트 발행 구현 예정
  }
}
