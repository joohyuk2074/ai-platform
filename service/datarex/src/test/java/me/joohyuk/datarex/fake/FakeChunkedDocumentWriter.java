package me.joohyuk.datarex.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.joohyuk.datarex.domain.port.out.storage.ChunkedDocumentWriter;
import me.joohyuk.datarex.domain.vo.DocumentContent;

/**
 * Fake implementation of ChunkedDocumentWriter for testing.
 *
 * This fake allows tests to:
 * - Capture all written chunks for verification
 * - Simulate write failures (IOException and other exceptions)
 * - Verify storage configuration
 */
public class FakeChunkedDocumentWriter implements ChunkedDocumentWriter {

  private final Map<String, WrittenData> storage = new HashMap<>();
  private RuntimeException failureToThrow;
  private int writeCallCount = 0;

  @Override
  public void write(List<DocumentContent> chunks, StorageConfig config) {
    writeCallCount++;

    // Simulate failure if configured
    if (failureToThrow != null) {
      throw failureToThrow;
    }

    // Store the chunks with their configuration
    String key = createKey(config);
    storage.put(key, new WrittenData(new ArrayList<>(chunks), config));
  }

  /**
   * Configure a failure to be thrown during write.
   */
  public void givenWriteFailure(RuntimeException exception) {
    this.failureToThrow = exception;
  }

  /**
   * Retrieve written chunks for a specific document.
   */
  public List<DocumentContent> getWrittenChunks(Long collectionId, Long documentId) {
    String key = createKey(collectionId, documentId);
    WrittenData data = storage.get(key);
    return data != null ? data.chunks : null;
  }

  /**
   * Retrieve the storage config used for a specific document.
   */
  public StorageConfig getStorageConfig(Long collectionId, Long documentId) {
    String key = createKey(collectionId, documentId);
    WrittenData data = storage.get(key);
    return data != null ? data.config : null;
  }

  /**
   * Check if chunks were written for a specific document.
   */
  public boolean hasWrittenChunks(Long collectionId, Long documentId) {
    String key = createKey(collectionId, documentId);
    return storage.containsKey(key);
  }

  /**
   * Get the number of chunks written for a specific document.
   */
  public int getChunkCount(Long collectionId, Long documentId) {
    List<DocumentContent> chunks = getWrittenChunks(collectionId, documentId);
    return chunks != null ? chunks.size() : 0;
  }

  /**
   * Returns the number of times write was called.
   */
  public int getWriteCallCount() {
    return writeCallCount;
  }

  /**
   * Get all written data (for debugging/inspection).
   */
  public Map<String, WrittenData> getAllWrittenData() {
    return new HashMap<>(storage);
  }

  /**
   * Reset the state of this fake.
   */
  public void reset() {
    storage.clear();
    failureToThrow = null;
    writeCallCount = 0;
  }

  private String createKey(StorageConfig config) {
    return createKey(
        config.collectionId().getValue(),
        config.documentId().getValue()
    );
  }

  private String createKey(Long collectionId, Long documentId) {
    return collectionId + ":" + documentId;
  }

  /**
   * Data class to hold written chunks and their config.
   */
  public record WrittenData(
      List<DocumentContent> chunks,
      StorageConfig config
  ) {
  }
}
