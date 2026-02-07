package me.joohyuk.datahub.infrastructure.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;
import me.joohyuk.datahub.application.dto.result.TransformDocumentResult;

/**
 * Passage 생성 요청 발행 API 응답 DTO
 *
 * <p><b>중요:</b> 이 응답은 "Kafka 이벤트 발행 성공 여부"를 나타내며,
 * 실제 Passage 생성 완료 여부는 나타내지 않습니다.
 *
 * <p>실제 Passage 생성 완료는 비동기로 처리되며,
 * Document 상태 조회 API를 통해 확인할 수 있습니다.
 *
 * @param collectionId 요청한 컬렉션 ID
 * @param totalDocumentsFound UPLOADED 상태인 총 Document 수
 * @param successfullyRequested 요청 이벤트 발행에 성공한 Document 수
 * @param failed 요청 이벤트 발행에 실패한 Document 수
 * @param requestedAt 요청 발행 시각
 * @param allRequestPublished 모든 요청이 발행되었는지 여부
 * @param documentResults 각 Document별 요청 발행 결과
 */
public record DocumentTransformRequestResponse(
    String collectionId,
    int totalDocumentsFound,
    int successfullyRequested,
    int failed,
    Instant requestedAt,
    boolean allRequestPublished,
    List<DocumentResult> documentResults
) {

    public static DocumentTransformRequestResponse from(TransformDocumentResult result) {
        List<DocumentResult> documentResults = result.documentResults().stream()
            .map(DocumentResult::from)
            .toList();

        return new DocumentTransformRequestResponse(
            String.valueOf(result.collectionId().getValue()),
            result.totalDocumentsFound(),
            result.successfullyRequested(),
            result.totalDocumentsFound() - result.successfullyRequested(),
            result.requestedAt(),
            result.isAllRequestPublished(),
            documentResults
        );
    }

    /**
     * 개별 Document의 요청 발행 결과
     *
     * @param documentId Document ID (문자열)
     * @param success 요청 발행 성공 여부
     * @param errorMessage 실패 시 에러 메시지
     */
    public record DocumentResult(
        String documentId,
        boolean success,
        String errorMessage
    ) {

        public static DocumentResult from(TransformDocumentResult.DocumentRequestResult result) {
            return new DocumentResult(
                String.valueOf(result.documentId().getValue()),
                result.success(),
                result.errorMessage()
            );
        }
    }
}
