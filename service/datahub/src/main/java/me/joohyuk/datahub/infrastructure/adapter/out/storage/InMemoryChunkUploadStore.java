package me.joohyuk.datahub.infrastructure.adapter.out.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import me.joohyuk.datahub.application.port.out.storage.ChunkUploadStore;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.vo.ChunkUploadSession;
import org.springframework.stereotype.Component;

/**
 * 메모리 기반 청크 업로드 저장소 구현체
 *
 * <p>개발/테스트 환경에서 사용하기 위한 구현체입니다. 프로덕션 환경에서는 Redis 또는 디스크 기반 구현체를
 * 사용해야 합니다.
 *
 * <p>세션과 청크 데이터 모두 ConcurrentHashMap으로 관리되어 동시 업로드에 안전합니다.
 */
@Component
public class InMemoryChunkUploadStore implements ChunkUploadStore {

  /** 업로드 ID -> 세션 정보 */
  private final ConcurrentHashMap<String, ChunkUploadSession> sessions = new ConcurrentHashMap<>();

  /** 업로드 ID -> (청크 인덱스 -> 청크 바이트 데이터) */
  private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, byte[]>> chunkDataMap =
      new ConcurrentHashMap<>();

  @Override
  public void createSession(ChunkUploadSession session) {
    sessions.put(session.getUploadId(), session);
    chunkDataMap.put(session.getUploadId(), new ConcurrentHashMap<>());
  }

  @Override
  public Optional<ChunkUploadSession> findSession(String uploadId) {
    return Optional.ofNullable(sessions.get(uploadId));
  }

  @Override
  public void storeChunk(String uploadId, int chunkIndex, InputStream chunkData) {
    ConcurrentHashMap<Integer, byte[]> chunks = chunkDataMap.get(uploadId);
    if (chunks == null) {
      throw new DatahubDomainException(
          "Upload session not found: " + uploadId,
          DatahubErrorCode.FILE_NOT_FOUND
      );
    }

    try {
      chunks.put(chunkIndex, chunkData.readAllBytes());
    } catch (IOException e) {
      throw new DatahubDomainException(
          "Failed to read chunk data for upload: " + uploadId,
          DatahubErrorCode.FILE_STORAGE_FAILED,
          e
      );
    }
  }

  @Override
  public InputStream assembleChunks(String uploadId) {
    ConcurrentHashMap<Integer, byte[]> chunks = chunkDataMap.get(uploadId);
    if (chunks == null) {
      throw new DatahubDomainException(
          "Upload session not found: " + uploadId,
          DatahubErrorCode.FILE_NOT_FOUND
      );
    }

    try {
      // TreeMap으로 청크 인덱스 순 정렬 후 순차 조합
      TreeMap<Integer, byte[]> sortedChunks = new TreeMap<>(chunks);
      ByteArrayOutputStream assembled = new ByteArrayOutputStream();
      for (byte[] chunkData : sortedChunks.values()) {
        assembled.write(chunkData);
      }
      return new ByteArrayInputStream(assembled.toByteArray());
    } catch (IOException e) {
      throw new DatahubDomainException(
          "Failed to assemble chunks for upload: " + uploadId,
          DatahubErrorCode.FILE_STORAGE_FAILED,
          e
      );
    }
  }

  @Override
  public void deleteSession(String uploadId) {
    sessions.remove(uploadId);
    chunkDataMap.remove(uploadId);
  }
}