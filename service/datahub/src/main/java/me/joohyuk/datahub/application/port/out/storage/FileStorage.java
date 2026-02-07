package me.joohyuk.datahub.application.port.out.storage;

import java.io.InputStream;
import com.spartaecommerce.domain.vo.Metadata;

/**
 * 파일 저장소 아웃바운드 포트
 *
 * 파일의 저장, 조회, 삭제를 추상화합니다.
 * 구현체는 Local, MinIO, S3 등 다양한 저장소를 선택할 수 있습니다.
 */
public interface FileStorage {

  /**
   * 파일을 저장하고 저장된 위치(키)를 반환합니다.
   *
   * @param inputStream 저장할 파일의 입력 스트림
   * @param metadata 파일 메타데이터 (파일명, 크기, 타입 등)
   * @param scope 논리적 저장 경로 접두사 (예: "collections/1")
   * @return 저장된 파일의 키 (예: "collections/1/1738000000_doc.md")
   * @throws FileStorageException 저장 중 오류 발생시
   */
  String store(InputStream inputStream, Metadata metadata, String scope);

  /**
   * 파일 키로 파일을 조회하여 입력 스트림을 반환합니다.
   *
   * @param fileKey 조회할 파일의 키
   * @return 파일 내용의 입력 스트림
   * @throws FileStorageException 파일이 존재하지 않거나 조회 중 오류 발생시
   */
  InputStream retrieve(String fileKey);

  /**
   * 파일을 삭제합니다.
   *
   * @param fileKey 삭제할 파일의 키
   * @throws FileStorageException 삭제 중 오류 발생시
   */
  void delete(String fileKey);

  /**
   * 파일이 존재하는지 확인합니다.
   *
   * @param fileKey 확인할 파일의 키
   * @return 파일 존재 여부
   */
  boolean exists(String fileKey);

  /**
   * 파일 저장소 관련 예외
   */
  class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
      super(message);
    }

    public FileStorageException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
