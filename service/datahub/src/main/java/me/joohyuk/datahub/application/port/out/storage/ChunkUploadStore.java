package me.joohyuk.datahub.application.port.out.storage;

import java.io.InputStream;
import java.util.Optional;
import me.joohyuk.datahub.domain.vo.ChunkUploadSession;

/**
 * 청크 업로드 저장소 아웃바운드 포트
 *
 * <p>멀티파트 청크 업로드의 세션과 청크 데이터를 관리합니다. 구현체는 메모리, 디스크, Redis 등 다양한
 * 저장소를 사용할 수 있습니다.
 */
public interface ChunkUploadStore {

  /**
   * 새로운 청크 업로드 세션을 생성합니다.
   *
   * @param session 생성할 세션 정보
   */
  void createSession(ChunkUploadSession session);

  /**
   * 업로드 ID로 세션을 조회합니다.
   *
   * @param uploadId 조회할 업로드 세션 ID
   * @return 세션이 존재하면 Optional로 반환, 없으면 빈 Optional
   */
  Optional<ChunkUploadSession> findSession(String uploadId);

  /**
   * 청크 데이터를 저장합니다.
   *
   * @param uploadId 업로드 세션 ID
   * @param chunkIndex 청크 인덱스 (0-based)
   * @param chunkData 청크 데이터 스트림
   * @throws FileStorage.FileStorageException 세션이 존재하지 않거나 저장 중 오류 발생시
   */
  void storeChunk(String uploadId, int chunkIndex, InputStream chunkData);

  /**
   * 저장된 모든 청크를 인덱스 순으로 조합하여 전체 파일의 InputStream을 반환합니다.
   *
   * @param uploadId 조합할 업로드 세션 ID
   * @return 조합된 파일의 InputStream
   * @throws FileStorage.FileStorageException 세션이 존재하지 않거나 조합 중 오류 발생시
   */
  InputStream assembleChunks(String uploadId);

  /**
   * 세션과 관련된 모든 데이터(세션 정보, 청크 데이터)를 삭제합니다.
   *
   * @param uploadId 삭제할 업로드 세션 ID
   */
  void deleteSession(String uploadId);
}