package me.joohyuk.datarex.fake;

import java.util.ArrayList;
import java.util.List;
import me.joohyuk.datarex.domain.port.out.message.DocumentTransformResultEventPublisher;
import me.joohyuk.messaging.events.DocumentTransformCompletedMessage;
import me.joohyuk.messaging.events.DocumentTransformFailedMessage;

/**
 * Fake implementation of DocumentTransformResultEventPublisher for testing.
 *
 * This fake allows tests to:
 * - Capture all published events for verification
 * - Verify event ordering and content
 * - Check which events were published
 */
public class FakeDocumentTransformResultEventPublisher implements
    DocumentTransformResultEventPublisher {

  private final List<DocumentTransformCompletedMessage> completedMessages = new ArrayList<>();
  private final List<DocumentTransformFailedMessage> failedMessages = new ArrayList<>();

  @Override
  public void publishCompleted(DocumentTransformCompletedMessage message) {
    completedMessages.add(message);
  }

  @Override
  public void publishFailed(DocumentTransformFailedMessage message) {
    failedMessages.add(message);
  }

  /**
   * Get all completed messages that were published.
   */
  public List<DocumentTransformCompletedMessage> getCompletedMessages() {
    return new ArrayList<>(completedMessages);
  }

  /**
   * Get the last completed message that was published.
   */
  public DocumentTransformCompletedMessage getLastCompletedMessage() {
    return completedMessages.isEmpty() ? null : completedMessages.get(completedMessages.size() - 1);
  }

  /**
   * Get all failed messages that were published.
   */
  public List<DocumentTransformFailedMessage> getFailedMessages() {
    return new ArrayList<>(failedMessages);
  }

  /**
   * Get the last failed message that was published.
   */
  public DocumentTransformFailedMessage getLastFailedMessage() {
    return failedMessages.isEmpty() ? null : failedMessages.get(failedMessages.size() - 1);
  }

  /**
   * Check if any completed message was published.
   */
  public boolean hasCompletedMessage() {
    return !completedMessages.isEmpty();
  }

  /**
   * Check if any failed message was published.
   */
  public boolean hasFailedMessage() {
    return !failedMessages.isEmpty();
  }

  /**
   * Get the count of completed messages published.
   */
  public int getCompletedMessageCount() {
    return completedMessages.size();
  }

  /**
   * Get the count of failed messages published.
   */
  public int getFailedMessageCount() {
    return failedMessages.size();
  }

  /**
   * Find a completed message for a specific document.
   */
  public DocumentTransformCompletedMessage findCompletedMessage(String documentId) {
    return completedMessages.stream()
        .filter(msg -> msg.documentId().equals(documentId))
        .findFirst()
        .orElse(null);
  }

  /**
   * Find a failed message for a specific document.
   */
  public DocumentTransformFailedMessage findFailedMessage(String documentId) {
    return failedMessages.stream()
        .filter(msg -> msg.documentId().equals(documentId))
        .findFirst()
        .orElse(null);
  }

  /**
   * Reset the state of this fake.
   */
  public void reset() {
    completedMessages.clear();
    failedMessages.clear();
  }
}
