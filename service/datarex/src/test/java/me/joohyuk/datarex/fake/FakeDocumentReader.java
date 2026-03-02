package me.joohyuk.datarex.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.joohyuk.datarex.application.dto.command.TransformDocumentCommand;
import me.joohyuk.datarex.application.port.out.storage.DocumentReader;
import me.joohyuk.datarex.domain.vo.DocumentContent;

/**
 * Fake implementation of DocumentReader for testing.
 *
 * This fake allows tests to:
 * - Configure documents to return for specific requests
 * - Simulate reading failures (IOException and other exceptions)
 * - Verify read operations were called with expected parameters
 */
public class FakeDocumentReader implements DocumentReader {

  private final Map<Long, List<DocumentContent>> documentStore = new HashMap<>();
  private final Map<Long, RuntimeException> failureStore = new HashMap<>();
  private TransformDocumentCommand lastRequest;

  @Override
  public List<DocumentContent> read(TransformDocumentCommand document) {
    lastRequest = document;

    Long documentId = document.documentId();

    // Check if failure is configured for this document
    if (failureStore.containsKey(documentId)) {
      throw failureStore.get(documentId);
    }

    // Return configured documents or empty list
    return documentStore.getOrDefault(documentId, List.of());
  }

  /**
   * Configure documents to be returned for a specific document ID.
   */
  public void givenDocuments(Long documentId, List<DocumentContent> documents) {
    documentStore.put(documentId, documents);
  }

  /**
   * Configure a failure to be thrown when reading a specific document ID.
   */
  public void givenReadFailure(Long documentId, RuntimeException exception) {
    failureStore.put(documentId, exception);
  }

  /**
   * Returns the last command received by this reader.
   */
  public TransformDocumentCommand getLastRequest() {
    return lastRequest;
  }

  /**
   * Checks if read was called.
   */
  public boolean wasReadCalled() {
    return lastRequest != null;
  }

  /**
   * Reset the state of this fake.
   */
  public void reset() {
    documentStore.clear();
    failureStore.clear();
    lastRequest = null;
  }
}
