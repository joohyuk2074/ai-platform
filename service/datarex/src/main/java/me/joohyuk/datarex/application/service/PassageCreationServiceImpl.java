package me.joohyuk.datarex.application.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.domain.entity.PassageCreationRequestedMessage;
import me.joohyuk.datarex.domain.entity.PassageCreationRequestedMessage.DocumentData;
import me.joohyuk.datarex.domain.exception.DatarexDomainException;
import me.joohyuk.datarex.domain.port.in.service.PassageCreationService;
import me.joohyuk.datarex.infrastructure.adapter.out.storage.MarkdownReader;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassageCreationServiceImpl implements PassageCreationService {

  private static final String BASE_STORAGE_PATH = "storage/documents/chunks";

  private final MarkdownReader markdownReader;
//  private final FileContentLoader fileContentLoader;
//  private final PassageChunker passageChunker;
//  private final PassageStore passageStore;
//  private final PassageResultEventPublisher resultEventPublisher;


  public void createPassages(PassageCreationRequestedMessage message) {
    DocumentData document = message.document();

    log.info(
        "Passage 생성 요청 수신 - documentId: {}, collectionId: {}, fileKey: {}, fileName: {}, contentType: {}, fileSize: {}",
        document.getDocumentId(),
        document.getCollectionId(),
        document.fileKey(),
        document.getFileName(),
        document.getContentType(),
        document.getFileSize()
    );

    // TODO: Spring AI를 사용한 청킹 구현 예정
    // 1. fileKey로 파일 컨텐츠 로드 (메타정보 포함)
    List<Document> documents = markdownReader.loadMarkdown(document);
    // 2. PassageChunker로 청킹
    TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(
        1000,
        400,
        10,
        5000,
        true
    );
    List<Document> splitDocuments = tokenTextSplitter.apply(documents);

    // 3. PassageStore에 저장
    String outputPath = String.format("%s/%d/%d/%s",
        BASE_STORAGE_PATH,
        document.getCollectionId(),
        document.getDocumentId(),
        document.getFileName()
    );

    // 디렉토리가 없으면 생성
    try {
      Path directoryPath = Paths.get(outputPath).getParent();
      if (directoryPath != null) {
        Files.createDirectories(directoryPath);
      }
    } catch (IOException e) {
      log.error("디렉토리 생성 실패: {}", outputPath, e);
      throw new DatarexDomainException("디렉토리 생성 실패", e);
    }

    FileDocumentWriter writer = new FileDocumentWriter(
        outputPath,
        true,
        MetadataMode.ALL,
        false
    );
    writer.accept(splitDocuments);

    // 4. 결과 이벤트 발행 (PassageResultEventPublisher)
  }
}
