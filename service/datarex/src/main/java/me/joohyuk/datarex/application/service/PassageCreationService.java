package me.joohyuk.datarex.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.domain.entity.PassageCreationRequestedMessage;
import me.joohyuk.datarex.domain.port.out.FileContentLoader;
import me.joohyuk.datarex.domain.port.out.PassageChunker;
import me.joohyuk.datarex.domain.port.out.PassageResultEventPublisher;
import me.joohyuk.datarex.domain.port.out.PassageStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassageCreationService {

//    private final FileContentLoader fileContentLoader;
//    private final PassageChunker passageChunker;
//    private final PassageStore passageStore;
//    private final PassageResultEventPublisher resultEventPublisher;

    public void createPassages(PassageCreationRequestedMessage message) {
        var doc = message.document();

        log.info("Passage 생성 요청 수신 - documentId: {}, collectionId: {}, fileKey: {}, fileName: {}, contentType: {}, fileSize: {}",
                doc.getDocumentId(),
                doc.getCollectionId(),
                doc.fileKey(),
                doc.getFileName(),
                doc.getContentType(),
                doc.getFileSize());

        // TODO: Spring AI를 사용한 청킹 구현 예정
        // 1. fileKey로 파일 컨텐츠 로드 (FileContentLoader)
        // 2. PassageChunker로 청킹
        // 3. PassageStore에 저장
        // 4. 결과 이벤트 발행 (PassageResultEventPublisher)
    }
}
