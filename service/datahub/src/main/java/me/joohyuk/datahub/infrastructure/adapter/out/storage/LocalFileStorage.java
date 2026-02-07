package me.joohyuk.datahub.infrastructure.adapter.storage;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.ContentHash;
import com.spartaecommerce.domain.vo.Metadata;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import me.joohyuk.datahub.application.port.out.storage.FileStorage;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.infrastructure.util.ContentHasher;
import me.joohyuk.datahub.infrastructure.util.ContentHasher.HashingInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalFileStorage implements FileStorage {

  private final Path baseDirectory;

  public LocalFileStorage(
      @Value("${file-storage.local.base-directory:./storage/documents}") String baseDirectory
  ) {
    this.baseDirectory = Paths.get(baseDirectory).toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.baseDirectory);
    } catch (IOException e) {
      throw new DatahubDomainException(
          "Failed to create base directory: " + baseDirectory,
          DatahubErrorCode.FILE_STORAGE_FAILED,
          e
      );
    }
  }

  @Override
  public FileStorageResult store(InputStream inputStream, Metadata metadata, CollectionId collectionId) {
    try {
      // 파일 키 생성: collections/{collectionId}/{timestamp}_{fileName}
      String fileKey = generateFileKey(metadata, collectionId);
      Path targetPath = baseDirectory.resolve(fileKey);

      // 디렉토리가 없으면 생성
      Files.createDirectories(targetPath.getParent());

      // Phase 1: 스트림을 감싸서 SHA-256 해시를 동시에 계산
      HashingInputStream hashingStream = ContentHasher.wrap(inputStream);

      // 파일 저장
      Files.copy(hashingStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

      // Phase 2: 스트림 소비 후 해시 추출
      ContentHash contentHash = hashingStream.getContentHash();

      return new FileStorageResult(fileKey, contentHash);
    } catch (IOException e) {
      throw new DatahubDomainException(
          "Failed to store file: " + metadata.fileName(),
          DatahubErrorCode.FILE_STORAGE_FAILED,
          e
      );
    }
  }

  @Override
  public InputStream retrieve(String fileKey) {
    try {
      Path filePath = baseDirectory.resolve(fileKey);
      if (!Files.exists(filePath)) {
        throw new DatahubDomainException(
            "File not found: " + fileKey,
            DatahubErrorCode.FILE_NOT_FOUND
        );
      }
      return Files.newInputStream(filePath);
    } catch (IOException e) {
      throw new DatahubDomainException(
          "Failed to retrieve file: " + fileKey,
          DatahubErrorCode.FILE_STORAGE_FAILED,
          e
      );
    }
  }

  @Override
  public void delete(String fileKey) {
    try {
      Path filePath = baseDirectory.resolve(fileKey);
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      throw new DatahubDomainException(
          "Failed to delete file: " + fileKey,
          DatahubErrorCode.FILE_DELETE_FAILED,
          e
      );
    }
  }

  @Override
  public boolean exists(String fileKey) {
    Path filePath = baseDirectory.resolve(fileKey);
    return Files.exists(filePath);
  }

  /**
   * 메타데이터와 collectionId로부터 파일 키를 생성합니다.
   * 형식: collections/{collectionId}/{timestamp}_{fileName}
   */
  private String generateFileKey(Metadata metadata, CollectionId collectionId) {
    String timestamp = String.valueOf(System.currentTimeMillis());
    String fileName = metadata.fileName();
    String scope = "collections/" + collectionId.getValue();
    return String.format("%s/%s_%s", scope, timestamp, fileName);
  }
}
