package me.joohyuk.datahub.fake;

import com.spartaecommerce.domain.vo.Metadata;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import me.joohyuk.datahub.application.port.out.storage.FileStorage;

public class MemoryFileFakeStorage implements FileStorage {

  private final Map<String, byte[]> store = new ConcurrentHashMap<>();
  private final Map<String, Metadata> metadataStore = new ConcurrentHashMap<>();
  private final AtomicInteger keyCounter = new AtomicInteger(1);

  private boolean throwOnDelete = false;
  private RuntimeException deleteException;

  @Override
  public String store(InputStream inputStream, Metadata metadata, String scope) {
    String fileKey = scope + "/test-file-" + keyCounter.getAndIncrement();
    try {
      byte[] data = inputStream.readAllBytes();
      store.put(fileKey, data);
      metadataStore.put(fileKey, metadata);
    } catch (Exception e) {
      throw new FileStorageException("Failed to store file", e);
    }
    return fileKey;
  }

  @Override
  public InputStream retrieve(String fileKey) {
    byte[] data = store.get(fileKey);
    if (data == null) {
      throw new FileStorageException("File not found: " + fileKey);
    }
    return new ByteArrayInputStream(data);
  }

  @Override
  public void delete(String fileKey) {
    if (throwOnDelete) {
      throw deleteException != null
          ? deleteException
          : new FileStorageException("Simulated delete failure");
    }
    store.remove(fileKey);
    metadataStore.remove(fileKey);
  }

  @Override
  public boolean exists(String fileKey) {
    return store.containsKey(fileKey);
  }

  // ─── 테스트 헬퍼 ──────────────────────────────────────────────

  /**
   * 현재 저장된 파일 수
   */
  public int size() {
    return store.size();
  }

  /**
   * 특정 파일 키에 대해 저장된 메타데이터 조회
   */
  public Metadata getMetadata(String fileKey) {
    return metadataStore.get(fileKey);
  }

  /**
   * delete() 호출 시 예외를 발생시키도록 설정 (보상 트랜잭션 실패 테스트용)
   */
  public void configureDeleteToFail() {
    this.throwOnDelete = true;
  }

  /**
   * delete() 호출 시 특정 예외를 발생시키도록 설정
   */
  public void configureDeleteToFail(RuntimeException exception) {
    this.throwOnDelete = true;
    this.deleteException = exception;
  }
}
