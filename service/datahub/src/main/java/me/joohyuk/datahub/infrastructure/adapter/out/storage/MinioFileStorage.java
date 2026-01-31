package me.joohyuk.datahub.infrastructure.adapter.storage;

import java.io.InputStream;
import me.joohyuk.datahub.domain.port.out.storage.FileStorage;
import com.spartaecommerce.domain.vo.Metadata;

/**
 * MinIO 기반 파일 저장소 구현체
 *
 * 온프레미스 환경에서 S3 호환 저장소로 사용하기 위한 구현체입니다.
 * TODO: MinIO SDK를 사용하여 연동 구현
 */
// @Component
// @Profile("minio")
public class MinioFileStorage implements FileStorage {

  // private final MinioClient minioClient;
  // private final String bucketName;

  @Override
  public String store(InputStream inputStream, Metadata metadata) {
    // TODO: MinIO 업로드 구현
    // 1. MinioClient를 사용하여 파일 업로드
    // 2. PutObjectArgs 생성
    // 3. 업로드 후 키 반환
    throw new UnsupportedOperationException("MinIO storage not implemented yet");
  }

  @Override
  public InputStream retrieve(String fileKey) {
    // TODO: MinIO 다운로드 구현
    // 1. GetObjectArgs 생성
    // 2. MinioClient를 사용하여 파일 다운로드
    // 3. InputStream 반환
    throw new UnsupportedOperationException("MinIO storage not implemented yet");
  }

  @Override
  public void delete(String fileKey) {
    // TODO: MinIO 삭제 구현
    // 1. RemoveObjectArgs 생성
    // 2. MinioClient를 사용하여 파일 삭제
    throw new UnsupportedOperationException("MinIO storage not implemented yet");
  }

  @Override
  public boolean exists(String fileKey) {
    // TODO: MinIO 존재 확인 구현
    // 1. StatObjectArgs 생성
    // 2. MinioClient를 사용하여 파일 존재 확인
    throw new UnsupportedOperationException("MinIO storage not implemented yet");
  }
}
