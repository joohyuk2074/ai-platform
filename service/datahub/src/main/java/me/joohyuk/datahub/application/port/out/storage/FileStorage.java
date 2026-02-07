package me.joohyuk.datahub.application.port.out.storage;

import com.spartaecommerce.domain.vo.ContentHash;
import com.spartaecommerce.domain.vo.Metadata;
import java.io.InputStream;

public interface FileStorage {

  /**
   * 파일을 저장하고 저장 결과(키 + 콘텐츠 해시)를 반환합니다.
   *
   * @param inputStream 저장할 파일의 입력 스트림
   * @param metadata    파일 메타데이터 (파일명, 크기, 타입 등)
   * @param scope       논리적 저장 경로 접두사 (예: "collections/1")
   * @return 저장된 파일의 키와 콘텐츠 해시
   * @throws com.spartaecommerce.exception.DomainException 저장 중 오류 발생시
   */
  FileStorageResult store(InputStream inputStream, Metadata metadata, String scope);

  /**
   * 파일 저장 결과를 나타내는 레코드입니다.
   *
   * @param fileKey 저장된 파일의 키 (예: "collections/1/1738000000_doc.md")
   * @param contentHash 파일의 SHA-256 콘텐츠 해시
   */
  record FileStorageResult(String fileKey, ContentHash contentHash) {}

  /**
   * 파일 키로 파일을 조회하여 입력 스트림을 반환합니다.
   *
   * @param fileKey 조회할 파일의 키
   * @return 파일 내용의 입력 스트림
   * @throws com.spartaecommerce.exception.DomainException 파일이 존재하지 않거나 조회 중 오류 발생시
   */
  InputStream retrieve(String fileKey);

  /**
   * 파일을 삭제합니다.
   *
   * @param fileKey 삭제할 파일의 키
   * @throws com.spartaecommerce.exception.DomainException 삭제 중 오류 발생시
   */
  void delete(String fileKey);

  /**
   * 파일이 존재하는지 확인합니다.
   *
   * @param fileKey 확인할 파일의 키
   * @return 파일 존재 여부
   */
  boolean exists(String fileKey);
}
