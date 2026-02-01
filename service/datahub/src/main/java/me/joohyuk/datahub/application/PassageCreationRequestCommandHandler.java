package me.joohyuk.datahub.application;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.util.DateTimeHolder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.PassageCreationRequestResult;
import me.joohyuk.datahub.application.dto.PassageCreationRequestResult.DocumentRequestResult;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.PassageCreationRequestEvent;
import me.joohyuk.datahub.domain.exception.IngestionDomainException;
import me.joohyuk.datahub.domain.port.out.message.publisher.PassageCreationRequestPublisher;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentCollectionRepository;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentRepository;
import me.joohyuk.datahub.domain.vo.DocumentStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Passage 생성 요청 이벤트 발행을 처리하는 Command Handler
 *
 * <p>이 핸들러는:
 * <ul>
 *   <li>Document 상태를 UPLOADED → PASSAGE_REQUESTED로 변경</li>
 *   <li>Kafka로 PassageCreationRequestEvent 발행</li>
 * </ul>
 *
 * <p><b>중요:</b> 실제 Passage 생성은 datarex 서비스에서 비동기로 처리되며,
 * 이 핸들러는 "요청 발행"까지만 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PassageCreationRequestCommandHandler {

    private final DocumentRepository documentRepository;
    private final DocumentCollectionRepository documentCollectionRepository;
    private final PassageCreationRequestPublisher passageCreationRequestPublisher;
    private final DateTimeHolder dateTimeHolder;

    /**
     * 컬렉션의 모든 UPLOADED 상태 Document에 대해 Passage 생성 요청 이벤트를 발행합니다.
     *
     * <p>각 Document에 대해:
     * <ol>
     *   <li>상태를 PASSAGE_REQUESTED로 변경</li>
     *   <li>PassageCreationRequestEvent를 Kafka로 발행</li>
     * </ol>
     *
     * <p>실패한 Document는 개별적으로 추적되며, 일부 실패 시에도
     * 성공한 Document들의 요청은 정상 발행됩니다.
     *
     * @param collectionId 요청할 컬렉션 ID
     * @return 요청 발행 결과 (발행 성공/실패 개수, 개별 결과)
     * @throws IngestionDomainException 컬렉션이 존재하지 않는 경우
     */
    public PassageCreationRequestResult requestPassageCreationByCollection(CollectionId collectionId) {
        if (!documentCollectionRepository.existsById(collectionId)) {
            throw new IngestionDomainException(
                "Failed to find Collection. collectionId: " + collectionId.getValue());
        }

        List<Document> documents =
            documentRepository.findByCollectionId(collectionId, DocumentStatus.UPLOADED);

        Instant now = dateTimeHolder.now();
        List<DocumentRequestResult> documentResults = new ArrayList<>();
        int successCount = 0;

        for (Document document : documents) {
            try {
                document.requestPassageCreation(now);
                documentRepository.save(document);

                // TODO: outbox 패턴 적용
                PassageCreationRequestEvent event =
                    new PassageCreationRequestEvent(document, dateTimeHolder.getCurrentDateTime());
                passageCreationRequestPublisher.publish(event);

                documentResults.add(DocumentRequestResult.success(document.getId()));
                successCount++;

                log.debug("Passage creation request event published for documentId={}", document.getId().getValue());
            } catch (Exception e) {
                log.error("Failed to publish passage creation request event for documentId={}", document.getId().getValue(), e);
                documentResults.add(DocumentRequestResult.failure(document.getId(), e.getMessage()));
            }
        }

        PassageCreationRequestResult result = new PassageCreationRequestResult(
            collectionId,
            documents.size(),
            successCount,
            now,
            documentResults
        );

        log.info("Passage creation request events published for collectionId={}: total={}, published={}, failed={}",
            collectionId.getValue(), result.totalDocumentsFound(), result.successfullyRequested(),
            result.totalDocumentsFound() - result.successfullyRequested());

        return result;
    }
}
