package me.joohyuk.datahub.application.dto;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.DocumentId;
import java.time.Instant;
import java.util.List;

/**
 * Passage 생성 요청 발행 결과를 나타내는 DTO
 *
 * <p><b>중요:</b> 이 Result는 "Kafka 이벤트 발행 성공 여부"를 나타내며,
 * 실제 Passage 생성 완료 여부는 나타내지 않습니다.
 *
 * <p>실제 Passage 생성 완료는 비동기로 처리되며,
 * {@code PassageCreationCompletedEvent} 또는 {@code PassageCreationFailedEvent}를
 * 통해 나중에 확인할 수 있습니다.
 *
 * <p>이 Result가 담는 정보:
 * <ul>
 *   <li>몇 개의 Document를 발견했는지</li>
 *   <li>몇 개의 요청 이벤트를 성공적으로 발행했는지</li>
 *   <li>각 Document별 요청 발행 성공/실패 여부</li>
 * </ul>
 *
 * @param collectionId 요청한 컬렉션 ID
 * @param totalDocumentsFound UPLOADED 상태인 총 Document 수
 * @param successfullyRequested 상태 변경 + 이벤트 발행에 성공한 Document 수
 * @param requestedAt 요청 발행 시각
 * @param documentResults 각 Document별 요청 발행 결과
 */
public record PassageCreationRequestResult(
    CollectionId collectionId,
    int totalDocumentsFound,
    int successfullyRequested,
    Instant requestedAt,
    List<DocumentRequestResult> documentResults
) {

    public boolean isAllRequestPublished() {
        return totalDocumentsFound == successfullyRequested;
    }

    public record DocumentRequestResult(
        DocumentId documentId,
        boolean success,
        String errorMessage
    ) {

        public static DocumentRequestResult success(DocumentId documentId) {
            return new DocumentRequestResult(documentId, true, null);
        }

        public static DocumentRequestResult failure(DocumentId documentId, String errorMessage) {
            return new DocumentRequestResult(documentId, false, errorMessage);
        }
    }
}
