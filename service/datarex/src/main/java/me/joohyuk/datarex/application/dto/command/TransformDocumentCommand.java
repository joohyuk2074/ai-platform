package me.joohyuk.datarex.application.dto.command;

import com.spartaecommerce.domain.vo.Metadata;

public record TransformDocumentCommand(
    Long sagaId,
    Long documentId,
    Long collectionId,
    String fileKey,
    String contentHash,
    Metadata metadata,
    int attempt
) {

}
