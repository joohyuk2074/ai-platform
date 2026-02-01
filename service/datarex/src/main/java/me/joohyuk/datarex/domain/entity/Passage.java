package me.joohyuk.datarex.domain.entity;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.DocumentId;

public record Passage(
    DocumentId documentId,
    CollectionId collectionId,
    String passageVersion,
    int chunkIndex,
    String text
) {

}
