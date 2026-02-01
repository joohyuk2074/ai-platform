package me.joohyuk.datahub.domain.vo;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 청크 업로드 세션 상태를 나타내는 값 객체
 *
 * <p>멀티파트 청크 업로드의 진행 상태를 추적합니다. 세션은 initiateChunkedUpload 시 생성되고,
 * completeChunkedUpload 후 정리됩니다.
 *
 * <p>receivedChunks는 ConcurrentHashMap.newKeySet()으로 구현되어 동시 청크 업로드에 안전합니다.
 */
public class ChunkUploadSession {

  private final String uploadId;
  private final com.spartaecommerce.domain.vo.CollectionId collectionId;
  private final String fileName;
  private final long totalSize;
  private final String contentType;
  private final Long uploadedBy;
  private final int totalChunks;
  private final int chunkSize;
  private final Set<Integer> receivedChunks;

  public ChunkUploadSession(
      String uploadId,
      com.spartaecommerce.domain.vo.CollectionId collectionId,
      String fileName,
      long totalSize,
      String contentType,
      Long uploadedBy,
      int totalChunks,
      int chunkSize
  ) {
    if (uploadId == null || uploadId.isBlank()) {
      throw new IllegalArgumentException("Upload ID cannot be empty");
    }
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("File name cannot be empty");
    }
    if (totalSize <= 0) {
      throw new IllegalArgumentException("Total size must be positive");
    }
    if (totalChunks <= 0) {
      throw new IllegalArgumentException("Total chunks must be positive");
    }
    if (chunkSize <= 0) {
      throw new IllegalArgumentException("Chunk size must be positive");
    }

    this.uploadId = uploadId;
    this.collectionId = collectionId;
    this.fileName = fileName;
    this.totalSize = totalSize;
    this.contentType = contentType;
    this.uploadedBy = uploadedBy;
    this.totalChunks = totalChunks;
    this.chunkSize = chunkSize;
    this.receivedChunks = ConcurrentHashMap.newKeySet();
  }

  public String getUploadId() {
    return uploadId;
  }

  public com.spartaecommerce.domain.vo.CollectionId getCollectionId() {
    return collectionId;
  }

  public String getFileName() {
    return fileName;
  }

  public long getTotalSize() {
    return totalSize;
  }

  public String getContentType() {
    return contentType;
  }

  public Long getUploadedBy() {
    return uploadedBy;
  }

  public int getTotalChunks() {
    return totalChunks;
  }

  public int getChunkSize() {
    return chunkSize;
  }

  public Set<Integer> getReceivedChunks() {
    return Collections.unmodifiableSet(receivedChunks);
  }

  /**
   * 청크를 수신 완료로 표시합니다.
   *
   * @param chunkIndex 수신된 청크 인덱스 (0-based)
   * @return 새로운 청크이면 true, 중복이면 false
   */
  public boolean markChunkReceived(int chunkIndex) {
    return receivedChunks.add(chunkIndex);
  }

  /** 모든 청크가 수신되었는지 확인합니다. */
  public boolean isAllChunksReceived() {
    return receivedChunks.size() == totalChunks;
  }

  /** 현재까지 수신된 청크 수를 반환합니다. */
  public int getReceivedChunkCount() {
    return receivedChunks.size();
  }

  /**
   * 주어진 청크 인덱스가 유효한 범위인지 확인합니다.
   *
   * @param chunkIndex 검증할 청크 인덱스
   * @return 0 이상이고 totalChunks 미만이면 true
   */
  public boolean isValidChunkIndex(int chunkIndex) {
    return chunkIndex >= 0 && chunkIndex < totalChunks;
  }
}