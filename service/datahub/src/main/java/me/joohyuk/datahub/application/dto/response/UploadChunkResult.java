package me.joohyuk.datahub.application.dto.response;

/**
 * 개별 청크 업로드 결과
 *
 * <p>클라이언트는 allChunksReceived가 true가 되면 completeChunkedUpload를 호출해야 합니다.
 *
 * @param uploadId 업로드 세션 ID
 * @param chunkIndex 방금 업로드된 청크 인덱스
 * @param receivedChunks 현재까지 수신된 청크 수
 * @param totalChunks 전체 청크 수
 * @param allChunksReceived 모든 청크가 수신되었는지 여부
 */
public record UploadChunkResult(
    String uploadId,
    int chunkIndex,
    int receivedChunks,
    int totalChunks,
    boolean allChunksReceived
) {}