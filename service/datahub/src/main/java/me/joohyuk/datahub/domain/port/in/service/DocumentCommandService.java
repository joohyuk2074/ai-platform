package me.joohyuk.datahub.domain.port.in.service;

import com.spartaecommerce.domain.vo.CollectionId;
import java.io.InputStream;
import me.joohyuk.datahub.application.dto.PassageCreationRequestResult;
import me.joohyuk.datahub.application.dto.request.UploadDocumentCommand;
import me.joohyuk.datahub.application.dto.response.UploadDocumentResult;

public interface DocumentCommandService {

    // ─── 단일 업로드 ────────────────────────────────────────────────

    /**
     * 파일을 한 번에 업로드합니다.
     *
     * @param command 파일 메타데이터
     * @param fileInputStream 파일 전체의 InputStream
     * @return 생성된 문서 정보
     */
    UploadDocumentResult uploadDocument(UploadDocumentCommand command, InputStream fileInputStream);

    // ─── Passage 생성 요청 ───────────────────────────────────────────

    /**
     * 특정 컬렉션에 속하는 {@code UPLOADED} 상태의 모든 Document에 대해 Transform 요청 이벤트를
     * Kafka로 발행합니다.
     *
     * <p>각 Document의 상태를 {@code UPLOADED → TRANSFORM_REQUESTED}로 전이시키고,
     * {@code PassageCreationRequestedEvent}를 Kafka 토픽 {@code passage.creation.requested}로
     * produce합니다. 이미 {@code TRANSFORM_REQUESTED} 이상의 상태인 Document는 건너뜁니다.
     *
     * @param collectionId Passage 생성을 요청할 컬렉션 ID
     * @return Passage 생성 요청 처리 결과 (처리된 문서 수, 성공/실패 정보)
     * @throws me.joohyuk.datahub.domain.exception.IngestionDomainException 컬렉션이 존재하지 않는
     *     경우
     */
    PassageCreationRequestResult requestPassageCreationByCollection(CollectionId collectionId);
}
