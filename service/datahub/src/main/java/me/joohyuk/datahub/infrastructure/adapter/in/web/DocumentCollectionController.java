package me.joohyuk.datahub.infrastructure.adapter.in.web;

import com.spartaecommerce.api.response.CommonResponse;
import com.spartaecommerce.domain.vo.CollectionId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.result.CreateDocumentCollectionResult;
import me.joohyuk.datahub.application.port.in.service.CreateDocumentCollectionUseCase;
import me.joohyuk.datahub.application.port.in.service.DeleteDocumentCollectionUseCase;
import me.joohyuk.datahub.application.port.in.service.TransformDocumentUseCase;
import me.joohyuk.datahub.application.port.in.service.UpdateDocumentCollectionUseCase;
import me.joohyuk.datahub.infrastructure.adapter.in.web.dto.CreateDocumentCollectionRequest;
import me.joohyuk.datahub.infrastructure.adapter.in.web.dto.UpdateDocumentCollectionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
public class DocumentCollectionController {

  private final CreateDocumentCollectionUseCase createDocumentCollectionUseCase;
  private final UpdateDocumentCollectionUseCase updateDocumentCollectionUseCase;
  private final DeleteDocumentCollectionUseCase deleteDocumentCollectionUseCase;
  private final TransformDocumentUseCase transformDocumentUseCase;

  @PostMapping
  public ResponseEntity<CommonResponse<CreateDocumentCollectionResult>> createCollection(
      @RequestHeader("X-Request-UserId") Long userId,
      @Valid @RequestBody CreateDocumentCollectionRequest request
  ) {
    log.info("User {} creating collection: {}", userId, request.name());

    CreateDocumentCollectionResult result =
        createDocumentCollectionUseCase.createCollection(request.toCommand(userId));

    CommonResponse<CreateDocumentCollectionResult> response = CommonResponse.success(result);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  @PutMapping("/{collectionId}")
  public ResponseEntity<CommonResponse<CreateDocumentCollectionResult>> updateCollection(
      @PathVariable Long collectionId,
      @Valid @RequestBody UpdateDocumentCollectionRequest request
  ) {
    CreateDocumentCollectionResult result = updateDocumentCollectionUseCase.updateCollection(
        request.toCommand(collectionId)
    );

    CommonResponse<CreateDocumentCollectionResult> response = CommonResponse.success(result);

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{collectionId}")
  public ResponseEntity<Void> deleteCollection(
      @PathVariable String collectionId
  ) {
    deleteDocumentCollectionUseCase.deleteCollection(CollectionId.of(collectionId));

    return ResponseEntity
        .noContent()
        .build();
  }

  @PostMapping("/{collectionId}/transform")
  public ResponseEntity<CommonResponse<Void>> transformDocuments(
      @PathVariable String collectionId
  ) {
    transformDocumentUseCase.transform(CollectionId.of(collectionId));

    return ResponseEntity.ok(CommonResponse.success(null));
  }
}
