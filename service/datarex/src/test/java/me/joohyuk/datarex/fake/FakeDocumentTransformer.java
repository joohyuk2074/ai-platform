package me.joohyuk.datarex.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.joohyuk.datarex.application.port.out.chunking.DocumentTransformer;
import me.joohyuk.datarex.domain.vo.DocumentContent;

/**
 * Fake implementation of DocumentTransformer for testing.
 *
 * This fake allows tests to:
 * - Simulate chunking behavior by returning predefined chunks
 * - Simulate transformation failures
 * - Verify transformation was called with expected inputs
 */
public class FakeDocumentTransformer implements DocumentTransformer {

  private final Map<String, List<DocumentContent>> chunkStore = new HashMap<>();
  private RuntimeException failureToThrow;
  private List<DocumentContent> lastInputDocuments;
  private ChunkingConfig lastConfig;
  private int transformCallCount = 0;

  @Override
  public List<DocumentContent> transform(
      List<DocumentContent> documents,
      ChunkingConfig config
  ) {
    transformCallCount++;
    lastInputDocuments = documents;
    lastConfig = config;

    // Simulate failure if configured
    if (failureToThrow != null) {
      throw failureToThrow;
    }

    // If specific chunks are configured for the input content, return them
    if (!documents.isEmpty()) {
      String firstContent = documents.get(0).content();
      if (chunkStore.containsKey(firstContent)) {
        return chunkStore.get(firstContent);
      }
    }

    // Default behavior: split each document into 2 chunks
    return createDefaultChunks(documents);
  }

  /**
   * Configure specific chunks to be returned for a given input content.
   */
  public void givenChunks(String inputContent, List<DocumentContent> chunks) {
    chunkStore.put(inputContent, chunks);
  }

  /**
   * Configure a failure to be thrown during transformation.
   */
  public void givenTransformFailure(RuntimeException exception) {
    this.failureToThrow = exception;
  }

  /**
   * Returns the last input documents received.
   */
  public List<DocumentContent> getLastInputDocuments() {
    return lastInputDocuments;
  }

  /**
   * Returns the last chunking config received.
   */
  public ChunkingConfig getLastConfig() {
    return lastConfig;
  }

  /**
   * Returns the number of times transform was called.
   */
  public int getTransformCallCount() {
    return transformCallCount;
  }

  /**
   * Reset the state of this fake.
   */
  public void reset() {
    chunkStore.clear();
    failureToThrow = null;
    lastInputDocuments = null;
    lastConfig = null;
    transformCallCount = 0;
  }

  /**
   * Default chunking behavior: split each document into 2 chunks.
   */
  private List<DocumentContent> createDefaultChunks(List<DocumentContent> documents) {
    List<DocumentContent> chunks = new ArrayList<>();
    for (int i = 0; i < documents.size(); i++) {
      DocumentContent doc = documents.get(i);
      String content = doc.content();
      int mid = content.length() / 2;

      // Create two chunks from each document
      chunks.add(new DocumentContent(
          content.substring(0, mid),
          Map.of("chunk_index", i * 2, "source_doc_index", i)
      ));
      chunks.add(new DocumentContent(
          content.substring(mid),
          Map.of("chunk_index", i * 2 + 1, "source_doc_index", i)
      ));
    }
    return chunks;
  }
}
