package me.joohyuk.datahub.infrastructure.adapter.in.web;

import com.spartaecommerce.api.response.CommonResponse;
import com.spartaecommerce.domain.vo.UserId;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.command.UploadDocumentCommand;
import me.joohyuk.datahub.application.dto.result.UploadDocumentResult;
import me.joohyuk.datahub.domain.port.in.service.DocumentCommandService;
import com.spartaecommerce.domain.vo.CollectionId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
   * @param file         업로드할 파일
   * @param collectionId 문서를 저장할 컬렉션 ID
   * @return 업로드된 문서 정보
   */
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<CommonResponse<UploadDocumentResult>> uploadDocument(
      @RequestHeader("X-Request-UserId") Long userId,
      @RequestParam("file") MultipartFile file,
      @RequestParam("collectionId") String collectionId
  ) throws IOException {
    log.info("User {} uploading file: {} (size: {} bytes) to collection: {}",
        userId,
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
        new UserId(userId)
//        passport.userId().getValue()
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
}
