package me.joohyuk.datahub.infrastructure.adapter.in.web;

import com.spartaecommerce.api.response.CommonResponse;
import com.spartaecommerce.domain.entity.Passport;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.request.InitiateChunkedUploadCommand;
import me.joohyuk.datahub.application.dto.request.UploadChunkCommand;
import me.joohyuk.datahub.application.dto.request.UploadDocumentCommand;
import me.joohyuk.datahub.application.dto.response.InitiateChunkedUploadResult;
import me.joohyuk.datahub.application.dto.response.UploadChunkResult;
import me.joohyuk.datahub.application.dto.response.UploadDocumentResult;
import me.joohyuk.datahub.domain.port.in.service.DocumentCommandService;
import me.joohyuk.datahub.domain.vo.CollectionId;
import me.joohyuk.datahub.infrastructure.adapter.web.auth.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

  private final DocumentCommandService documentCommandService;

  // ─── 단일 업로드 ────────────────────────────────────────────────

  /**
   * 문서 파일을 한 번에 업로드합니다.
   *
   * @param passport 인증된 사용자 정보
   * @param file 업로드할 파일
   * @param collectionId 문서를 저장할 컬렉션 ID
   * @return 업로드된 문서 정보
   */
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<CommonResponse<UploadDocumentResult>> uploadDocument(
      @AuthenticatedUser Passport passport,
      @RequestParam("file") MultipartFile file,
      @RequestParam("collectionId") String collectionId
  ) throws IOException {
    log.info("User {} (username: {}) uploading file: {} (size: {} bytes) to collection: {}",
        passport.userId().getValue(),
        passport.username(),
        file.getOriginalFilename(),
        file.getSize(),
        collectionId);

    if (file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be empty");
    }

    UploadDocumentCommand command = new UploadDocumentCommand(
        CollectionId.of(collectionId),
        file.getOriginalFilename(),
        file.getSize(),
        file.getContentType(),
        passport.userId().getValue()
    );

    UploadDocumentResult result = documentCommandService.uploadDocument(
        command,
        file.getInputStream()
    );

    log.info("File uploaded successfully. Document ID: {}", result.documentId());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(CommonResponse.success(result));
  }

  // ─── 멀티파트 청크 업로드 (3단계 프로토콜) ──────────────────────

  /**
   * Step 1. 청크 업로드 세션을 시작합니다.
   *
   * <p>클라이언트는 반환된 {@code uploadId}와 {@code chunkSize}를 기반으로 파일을 분할하여 Step 2를
   * 반복 호출해야 합니다.
   *
   * @param passport 인증된 사용자 정보
   * @param collectionId 문서를 저장할 컬렉션 ID
   * @param fileName 원본 파일명
   * @param totalSize 파일 전체 크기 (바이트)
   * @param contentType MIME 타입
   * @return 업로드 세션 정보 (uploadId, chunkSize, totalChunks)
   */
  @PostMapping("/upload/chunked/initiate")
  public ResponseEntity<CommonResponse<InitiateChunkedUploadResult>> initiateChunkedUpload(
      @AuthenticatedUser Passport passport,
      @RequestParam("collectionId") String collectionId,
      @RequestParam("fileName") String fileName,
      @RequestParam("totalSize") long totalSize,
      @RequestParam("contentType") String contentType
  ) {
    log.info("User {} initiating chunked upload: fileName={}, totalSize={} bytes, collectionId={}",
        passport.userId().getValue(), fileName, totalSize, collectionId);

    InitiateChunkedUploadCommand command = new InitiateChunkedUploadCommand(
        CollectionId.of(collectionId),
        fileName,
        totalSize,
        contentType,
        passport.userId().getValue()
    );

    InitiateChunkedUploadResult result = documentCommandService.initiateChunkedUpload(command);

    log.info("Chunked upload initiated: uploadId={}, totalChunks={}",
        result.uploadId(), result.totalChunks());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(CommonResponse.success(result));
  }

  /**
   * Step 2. 개별 청크를 업로드합니다.
   *
   * <p>클라이언트는 이 엔드포인트를 {@code totalChunks}만큼 반복 호출합니다. 응답의 {@code
   * allChunksReceived}가 {@code true}이면 Step 3을 호출해야 합니다. 중복 청크 재전송은 멱등적으로
   * 처리됩니다.
   *
   * @param passport 인증된 사용자 정보
   * @param uploadId initiateChunkedUpload에서 반환된 세션 ID
   * @param chunkIndex 현재 청크의 인덱스 (0-based)
   * @param chunk 청크 파일 데이터
   * @return 청크 업로드 결과 및 전체 진행 상태
   */
  @PostMapping(value = "/upload/chunked/{uploadId}/chunk",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<CommonResponse<UploadChunkResult>> uploadChunk(
      @AuthenticatedUser Passport passport,
      @PathVariable String uploadId,
      @RequestParam("chunkIndex") int chunkIndex,
      @RequestParam("chunk") MultipartFile chunk
  ) throws IOException {
    log.info("User {} uploading chunk: uploadId={}, chunkIndex={}, size={} bytes",
        passport.userId().getValue(), uploadId, chunkIndex, chunk.getSize());

    if (chunk.isEmpty()) {
      throw new IllegalArgumentException("Chunk cannot be empty");
    }

    UploadChunkCommand command = new UploadChunkCommand(uploadId, chunkIndex);

    UploadChunkResult result = documentCommandService.uploadChunk(command, chunk.getInputStream());

    return ResponseEntity
        .ok(CommonResponse.success(result));
  }

  /**
   * Step 3. 모든 청크를 조합하여 문서를 생성합니다.
   *
   * <p>저장된 청크들을 인덱스 순으로 조합한 후 파일 저장소에 저장하고 Document 엔티티를 생성합니다.
   * 완료 후 임시 세션과 청크 데이터는 자동 정리됩니다.
   *
   * @param passport 인증된 사용자 정보
   * @param uploadId 완료할 업로드 세션 ID
   * @return 생성된 문서 정보
   */
  @PostMapping("/upload/chunked/{uploadId}/complete")
  public ResponseEntity<CommonResponse<UploadDocumentResult>> completeChunkedUpload(
      @AuthenticatedUser Passport passport,
      @PathVariable String uploadId
  ) {
    log.info("User {} completing chunked upload: uploadId={}",
        passport.userId().getValue(), uploadId);

    UploadDocumentResult result = documentCommandService.completeChunkedUpload(uploadId);

    log.info("Chunked upload completed: uploadId={}, documentId={}",
        uploadId, result.documentId());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(CommonResponse.success(result));
  }
}
