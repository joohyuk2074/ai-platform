package me.joohyuk.datahub.infrastructure.adapter.in.web;

import com.spartaecommerce.api.response.CommonResponse;
import com.spartaecommerce.domain.entity.Passport;
import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.result.TransformDocumentResult;
import me.joohyuk.datahub.application.dto.command.CreateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.command.UpdateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.result.CreateDocumentCollectionResult;
import me.joohyuk.datahub.domain.port.in.service.DocumentCollectionCommandService;
import me.joohyuk.datahub.domain.port.in.service.DocumentCommandService;
import me.joohyuk.datahub.infrastructure.adapter.in.web.dto.DocumentTransformRequestResponse;
import me.joohyuk.datahub.infrastructure.adapter.web.auth.AuthenticatedUser;
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

    private final DocumentCollectionCommandService documentCollectionCommandService;
    private final DocumentCommandService documentCommandService;

    @PostMapping
    public ResponseEntity<CommonResponse<CreateDocumentCollectionResult>> createCollection(
//      @AuthenticatedUser Passport passport,
        @RequestHeader("X-Request-UserId") Long userId,
        @RequestBody CreateDocumentCollectionCommand command
    ) {
        log.info("User {} creating collection: {}", userId, command.name());

        CreateDocumentCollectionResult result =
            documentCollectionCommandService.createCollection(new UserId(userId), command);

        CommonResponse<CreateDocumentCollectionResult> response = CommonResponse.success(result);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @PutMapping("/{collectionId}")
    public ResponseEntity<CommonResponse<CreateDocumentCollectionResult>> updateCollection(
        @AuthenticatedUser Passport passport,
        @PathVariable String collectionId,
        @RequestBody UpdateDocumentCollectionCommand command
    ) {
        log.info("User {} (username: {}) updating collection: {}",
            passport.userId().getValue(), passport.username(), collectionId);

        CreateDocumentCollectionResult result =
            documentCollectionCommandService.updateCollection(CollectionId.of(collectionId), command);

        CommonResponse<CreateDocumentCollectionResult> response = CommonResponse.success(result);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteCollection(
        @AuthenticatedUser Passport passport,
        @PathVariable String collectionId
    ) {
        log.info("User {} deleting collection: {}", passport.userId().getValue(), collectionId);

        documentCollectionCommandService.deleteCollection(CollectionId.of(collectionId));

        return ResponseEntity
            .noContent()
            .build();
    }

    @PostMapping("/{collectionId}/transform")
    public ResponseEntity<CommonResponse<DocumentTransformRequestResponse>> transformDocuments(
        @RequestHeader("X-Request-UserId") Long userId,
        @PathVariable String collectionId
    ) {
        log.info("User {} requesting document transformation for collectionId={}", userId, collectionId);

        TransformDocumentResult result =
            documentCommandService.requestPassageCreationByCollection(CollectionId.of(collectionId));

        log.info("Document transformation request completed: collectionId={}, total={}, successful={}, failed={}",
            collectionId, result.totalDocumentsFound(), result.successfullyRequested(),
            result.totalDocumentsFound() - result.successfullyRequested());

        DocumentTransformRequestResponse response = DocumentTransformRequestResponse.from(result);

        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
