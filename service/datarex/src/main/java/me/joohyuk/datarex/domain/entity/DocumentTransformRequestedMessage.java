package me.joohyuk.datarex.domain.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Document Transform 요청 메시지.
 *
 * <p>datahub 서비스의 DocumentTransformRequestEvent를 Kafka를 통해 받습니다.
 * datarex 서비스는 이 메시지를 소비하여 실제 문서 청킹(chunking) 작업을 수행합니다.
 */
public record DocumentTransformRequestedMessage(
    DocumentData document,
    LocalDateTime createdAt
) {

    /**
     * Document 정보를 담는 중첩 레코드.
     */
    public record DocumentData(
        IdWrapper id,
        IdWrapper collectionId,
        String fileKey,
        ContentHashWrapper contentHash,
        MetadataData metadata,
        String status,
        int attempt,
        String lastErrorCode,
        String lastErrorMessage,
        int passageCount,
        String lastResultEventId,
        Instant createdAt,
        Instant updatedAt
    ) {

        // Document Transform에 필요한 주요 정보 편의 메서드
        public Long getDocumentId() {
            return id != null ? id.value() : null;
        }

        public Long getCollectionId() {
            return collectionId != null ? collectionId.value() : null;
        }

        public String getFileName() {
            return metadata != null ? metadata.fileName() : null;
        }

        public String getContentType() {
            return metadata != null ? metadata.contentType() : null;
        }

        public Long getFileSize() {
            return metadata != null ? metadata.fileSize() : null;
        }

        public String getContentHashValue() {
            return contentHash != null ? contentHash.value() : null;
        }
    }

    /**
     * ID 래퍼 (DocumentId, CollectionId 등).
     */
    public record IdWrapper(Long value) {

    }

    /**
     * ContentHash 래퍼.
     */
    public record ContentHashWrapper(String value) {

    }

    /**
     * Metadata 정보.
     */
    public record MetadataData(
        String fileName,
        Long fileSize,
        String contentType,
        IdWrapper uploadedBy,
        String source,
        String author,
        List<String> tags
    ) {

    }
}
