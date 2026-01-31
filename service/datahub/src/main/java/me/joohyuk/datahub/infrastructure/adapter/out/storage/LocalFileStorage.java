package me.joohyuk.datahub.infrastructure.adapter.storage;

import com.spartaecommerce.domain.vo.Metadata;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import me.joohyuk.datahub.domain.port.out.storage.FileStorage;
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
      throw new FileStorageException("Failed to create base directory: " + baseDirectory, e);
    }
  }

  @Override
  public String store(InputStream inputStream, Metadata metadata) {
    try {
      // 파일 키 생성: documents/{fileName}
      String fileKey = generateFileKey(metadata);
      Path targetPath = baseDirectory.resolve(fileKey);

      // 디렉토리가 없으면 생성
      Files.createDirectories(targetPath.getParent());

      // 파일 저장
      Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

      return fileKey;
    } catch (IOException e) {
      throw new FileStorageException("Failed to store file: " + metadata.fileName(), e);
    }
  }

  @Override
  public InputStream retrieve(String fileKey) {
    try {
      Path filePath = baseDirectory.resolve(fileKey);
      if (!Files.exists(filePath)) {
        throw new FileStorageException("File not found: " + fileKey);
      }
      return Files.newInputStream(filePath);
    } catch (IOException e) {
      throw new FileStorageException("Failed to retrieve file: " + fileKey, e);
    }
  }

  @Override
  public void delete(String fileKey) {
    try {
      Path filePath = baseDirectory.resolve(fileKey);
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      throw new FileStorageException("Failed to delete file: " + fileKey, e);
    }
  }

  @Override
  public boolean exists(String fileKey) {
    Path filePath = baseDirectory.resolve(fileKey);
    return Files.exists(filePath);
  }

  /**
   * 메타데이터로부터 파일 키를 생성합니다. 형식: documents/{timestamp}_{fileName}
   */
  private String generateFileKey(Metadata metadata) {
    String timestamp = String.valueOf(System.currentTimeMillis());
    String fileName = metadata.fileName();
    return String.format("documents/%s_%s", timestamp, fileName);
  }
}
