package me.joohyuk.datahub.infrastructure.adapter.storage;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.Metadata;
import java.io.InputStream;
import me.joohyuk.datahub.application.port.out.storage.FileStorage;

/**
 * AWS S3 기반 파일 저장소 구현체
 *
 * 프로덕션 환경에서 사용하기 위한 구현체입니다.
 * TODO: AWS SDK를 사용하여 S3 연동 구현
 */
// @Component
// @Profile("prod")
public class S3FileStorage implements FileStorage {

  // private final S3Client s3Client;
  // private final String bucketName;

  @Override
  public FileStorageResult store(InputStream inputStream, Metadata metadata, CollectionId collectionId) {
    // TODO: S3 업로드 구현
    // 1. ContentHasher.wrap()으로 스트림 감싸기
    // 2. S3Client를 사용하여 파일 업로드
    // 3. PutObjectRequest 생성 (경로: collections/{collectionId}/{timestamp}_{fileName})
    // 4. 업로드 후 S3 키와 contentHash 반환
    throw new UnsupportedOperationException("S3 storage not implemented yet");
  }

  @Override
  public InputStream retrieve(String fileKey) {
    // TODO: S3 다운로드 구현
    // 1. GetObjectRequest 생성
    // 2. S3Client를 사용하여 파일 다운로드
    // 3. InputStream 반환
    throw new UnsupportedOperationException("S3 storage not implemented yet");
  }

  @Override
  public void delete(String fileKey) {
    // TODO: S3 삭제 구현
    // 1. DeleteObjectRequest 생성
    // 2. S3Client를 사용하여 파일 삭제
    throw new UnsupportedOperationException("S3 storage not implemented yet");
  }

  @Override
  public boolean exists(String fileKey) {
    // TODO: S3 존재 확인 구현
    // 1. HeadObjectRequest 생성
    // 2. S3Client를 사용하여 파일 존재 확인
    throw new UnsupportedOperationException("S3 storage not implemented yet");
  }
}
