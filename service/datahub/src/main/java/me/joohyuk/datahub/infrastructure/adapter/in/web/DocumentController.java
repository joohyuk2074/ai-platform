package me.joohyuk.datahub.infrastructure.adapter.in.web;

import com.spartaecommerce.api.response.CommonResponse;
import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.UserId;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.command.UploadDocumentCommand;
import me.joohyuk.datahub.application.dto.result.UploadDocumentResult;
import me.joohyuk.datahub.application.port.in.service.UploadDocumentUseCase;
import me.joohyuk.datahub.domain.exception.DatahubDomainErrorCode;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DocumentController {

  private final UploadDocumentUseCase uploadDocumentUseCase;

  @PostMapping(
      value = "/collections/{collectionId}/documents",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public ResponseEntity<CommonResponse<UploadDocumentResult>> uploadDocument(
      @PathVariable Long collectionId,
      @RequestHeader("X-Request-UserId") Long userId,
      @RequestParam("file") MultipartFile file
  ) throws IOException {
    log.info("User {} uploading file: {} (size: {} bytes) to collection: {}",
        userId,
        file.getOriginalFilename(),
        file.getSize(),
        collectionId);

    if (file.isEmpty()) {
      throw new DatahubDomainException(
          "File cannot be empty",
          DatahubDomainErrorCode.INVALID_FILE_EMPTY
      );
    }

    UploadDocumentCommand command = new UploadDocumentCommand(
        CollectionId.of(collectionId),
        file.getOriginalFilename(),
        file.getSize(),
        file.getContentType(),
        new UserId(userId)
    );

    UploadDocumentResult result = uploadDocumentUseCase.uploadDocument(
        command,
        file.getInputStream()
    );

    log.info("File uploaded successfully. Document ID: {}", result.documentId());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(CommonResponse.success(result));
  }
}
