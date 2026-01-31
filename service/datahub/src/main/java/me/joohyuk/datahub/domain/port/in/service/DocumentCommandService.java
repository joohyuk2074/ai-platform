package me.joohyuk.datahub.domain.port.in.service;

import java.io.InputStream;
import me.joohyuk.datahub.application.dto.request.InitiateChunkedUploadCommand;
import me.joohyuk.datahub.application.dto.request.UploadChunkCommand;
import me.joohyuk.datahub.application.dto.request.UploadDocumentCommand;
import me.joohyuk.datahub.application.dto.response.InitiateChunkedUploadResult;
import me.joohyuk.datahub.application.dto.response.UploadChunkResult;
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

  // ─── 멀티파트 청크 업로드 (3단계 프로토콜) ──────────────────────

  /**
   * Step 1. 청크 업로드 세션을 시작합니다.
   *
   * <p>파일 메타데이터를 기반으로 업로드 세션을 생성하고, 클라이언트가 후속 청크를 업로드할 수 있는
   * uploadId와 청크 분할 정보를 반환합니다.
   *
   * @param command 파일 메타데이터 (파일명, 크기, 타입 등)
   * @return uploadId, chunkSize, totalChunks 등 후속 업로드에 필요한 정보
   */
  InitiateChunkedUploadResult initiateChunkedUpload(InitiateChunkedUploadCommand command);

  /**
   * Step 2. 개별 청크를 업로드합니다.
   *
   * <p>클라이언트는 initiateChunkedUpload에서 받은 uploadId와 chunkSize를 기준으로 파일을 분할하여
   * 각 청크를 순차 또는 병렬로 업로드합니다. 중복 청크 업로드는 멱등적으로 처리됩니다.
   *
   * @param command uploadId와 chunkIndex
   * @param chunkInputStream 해당 청크의 바이트 데이터 스트림
   * @return 현재 진행 상태 (수신된 청크 수, 완료 여부 등)
   */
  UploadChunkResult uploadChunk(UploadChunkCommand command, InputStream chunkInputStream);

  /**
   * Step 3. 모든 청크가 업로드된 후 파일을 조합하여 문서를 생성합니다.
   *
   * <p>저장된 청크들을 인덱스 순으로 조합한 후, 단일 업로드와 동일한 저장 및 이벤트 발행 로직을
   * 실행합니다. 완료 후 세션과 임시 청크 데이터는 정리됩니다.
   *
   * @param uploadId 완료할 업로드 세션 ID
   * @return 생성된 문서 정보
   * @throws me.joohyuk.datahub.domain.exception.IngestionDomainException 세션이 없거나 청크가
   *     부족한 경우
   */
  UploadDocumentResult completeChunkedUpload(String uploadId);
}
