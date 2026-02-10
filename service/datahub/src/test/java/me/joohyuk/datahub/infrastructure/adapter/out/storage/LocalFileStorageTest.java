package me.joohyuk.datahub.infrastructure.adapter.out.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.Metadata;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import me.joohyuk.datahub.application.dto.result.FileStorageResult;
import me.joohyuk.datahub.application.port.out.storage.FileStorage;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("LocalFileStorage.store() 테스트")
class LocalFileStorageTest {

  @TempDir
  private Path tempDir;

  private LocalFileStorage storage;

  @BeforeEach
  void setUp() {
    storage = new LocalFileStorage(tempDir.toString());
  }

  @Test
  @DisplayName("정상 저장 시 파일 키와 콘텐츠 해시를 반환한다")
  void store_정상저장_시_파일키와_해시를_반환한다() {
    InputStream inputStream =
        new ByteArrayInputStream("테스트 내용".getBytes(StandardCharsets.UTF_8));
    Metadata metadata = Metadata.forUpload("test.md", 12L, "text/markdown", 1L);

    FileStorageResult result = storage.store(inputStream, metadata, CollectionId.of(1L));

    assertNotNull(result);
    assertNotNull(result.fileKey());
    assertFalse(result.fileKey().isBlank());
    assertNotNull(result.contentHash());
    assertNotNull(result.contentHash().getValue());
  }

  @Test
  @DisplayName("반환된 파일 키의 형식이 collections/{collectionId}/{timestamp}_{fileName}이다")
  void store_파일키_형식이_올바름() {
    InputStream inputStream = new ByteArrayInputStream("내용".getBytes(StandardCharsets.UTF_8));
    Metadata metadata = Metadata.forUpload("report.pdf", 2L, "application/pdf", 1L);

    FileStorageResult result = storage.store(inputStream, metadata, CollectionId.of(1L));

    assertTrue(
        result.fileKey().matches("collections/1/\\d+_report\\.pdf"),
        "파일 키 형식이 'collections/{collectionId}/{timestamp}_{fileName}'이어야 한다. 실제값: " + result.fileKey());
  }

  @Test
  @DisplayName("저장된 파일의 내용이 입력 스트림의 내용과 동일하다")
  void store_파일_내용이_올바르게_저장됨() throws IOException {
    String content = "저장될 파일 내용입니다.";
    byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
    InputStream inputStream = new ByteArrayInputStream(contentBytes);
    Metadata metadata = Metadata.forUpload("content.txt", (long) contentBytes.length, "text/plain",
        1L);

    FileStorageResult result = storage.store(inputStream, metadata, CollectionId.of(1L));

    Path storedFilePath = tempDir.resolve(result.fileKey());
    assertTrue(Files.exists(storedFilePath), "파일이 디스크에 존재해야 한다");
    String storedContent = new String(Files.readAllBytes(storedFilePath), StandardCharsets.UTF_8);
    assertEquals(content, storedContent);
  }

  @Test
  @DisplayName("baseDirectory 아래에 collections/{collectionId} 하위 디렉토리를 생성한다")
  void store_scope_하위_디렉토리_생성() {
    InputStream inputStream = new ByteArrayInputStream("내용".getBytes(StandardCharsets.UTF_8));
    Metadata metadata = Metadata.forUpload("dir-test.md", 2L, "text/markdown", 1L);

    FileStorageResult result = storage.store(inputStream, metadata, CollectionId.of(1L));

    Path collectionsDir = tempDir.resolve("collections/1");
    assertTrue(Files.isDirectory(collectionsDir), "collections/1 디렉토리가 생성되어야 한다");
  }

  @Test
  @DisplayName("빈 파일도 성공적으로 저장된다")
  void store_빈_파일_저장_가능() throws IOException {
    InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
    Metadata metadata = Metadata.forUpload("empty.txt", 0L, "text/plain", 1L);

    FileStorageResult result = storage.store(emptyStream, metadata, CollectionId.of(1L));

    Path storedFilePath = tempDir.resolve(result.fileKey());
    assertTrue(Files.exists(storedFilePath), "빈 파일도 디스크에 존재해야 한다");
    assertEquals(0L, Files.size(storedFilePath), "저장된 파일의 크기가 0이어야 한다");
  }

  @Test
  @DisplayName("InputStream 읽기 중 IOException 발생 시 FileStorageException이 발생한다")
  void store_IOException_발생_시_FileStorageException_발생() {
    InputStream brokenStream =
        new InputStream() {
          @Override
          public int read(byte[] b, int off, int len) throws IOException {
            throw new IOException("스트림 읽기 오류");
          }

          @Override
          public int read() throws IOException {
            throw new IOException("스트림 읽기 오류");
          }
        };
    Metadata metadata = Metadata.forUpload("broken.md", 10L, "text/markdown", 1L);

    DatahubDomainException exception =
        assertThrows(
            DatahubDomainException.class,
            () -> storage.store(brokenStream, metadata, CollectionId.of(1L)));

    assertTrue(
        exception.getMessage().contains("Failed to store file: broken.md"),
        "예외 메시지에 파일명이 포함되어야 한다. 실제값: " + exception.getMessage());
    assertInstanceOf(IOException.class, exception.getCause());
  }
}