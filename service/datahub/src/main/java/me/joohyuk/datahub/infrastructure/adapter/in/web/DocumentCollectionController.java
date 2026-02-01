package me.joohyuk.datahub.infrastructure.adapter.web;

import com.spartaecommerce.api.response.CommonResponse;
import com.spartaecommerce.domain.entity.Passport;
import com.spartaecommerce.domain.vo.UserId;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.request.CreateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.request.UpdateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.response.CreateDocumentCollectionResult;
import me.joohyuk.datahub.domain.port.in.service.DocumentCollectionCommandService;
import me.joohyuk.datahub.domain.port.in.service.DocumentCommandService;
import me.joohyuk.datahub.domain.vo.CollectionId;
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

  @PostMapping("/{collectionId}/passages/request")
  public ResponseEntity<CommonResponse<Map<String, Object>>> requestPassageCreation(
      @RequestHeader("X-Request-UserId") Long userId,
      @PathVariable String collectionId
  ) {
    log.info("User {} requesting passage creation for collectionId={}", userId, collectionId);

    int publishedCount =
        documentCommandService.requestPassageCreationByCollection(CollectionId.of(collectionId));

    log.info("Passage creation request completed: collectionId={}, publishedCount={}",
        collectionId, publishedCount);

    Map<String, Object> body = Map.of(
        "collectionId", collectionId,
        "publishedCount", publishedCount
    );

    return ResponseEntity.ok(CommonResponse.success(body));
  }
}
