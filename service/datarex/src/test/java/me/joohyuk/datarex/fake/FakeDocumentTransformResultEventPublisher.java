package me.joohyuk.datarex.fake;

import java.util.ArrayList;
import java.util.List;
import me.joohyuk.datarex.application.port.out.message.DocumentTransformResultEventPublisher;
import me.joohyuk.messaging.events.TransformDocumentCompletedEvent;

/**
 * Fake implementation of DocumentTransformResultEventPublisher for testing.
 * <p>
 * This fake allows tests to: - Capture all published events for verification - Verify event
 * ordering and content - Check which events were published
 */
public class FakeDocumentTransformResultEventPublisher implements
    DocumentTransformResultEventPublisher {

  private final List<TransformDocumentCompletedEvent> completedMessages = new ArrayList<>();

  @Override
  public void publishCompleted(TransformDocumentCompletedEvent message) {
    completedMessages.add(message);
  }

  /**
   * Get all completed messages that were published.
   */
  public List<TransformDocumentCompletedEvent> getCompletedMessages() {
    return new ArrayList<>(completedMessages);
  }

  /**
   * Get the last completed message that was published.
   */
  public TransformDocumentCompletedEvent getLastCompletedMessage() {
    return completedMessages.isEmpty() ? null : completedMessages.get(completedMessages.size() - 1);
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
    return completedMessages.stream()
        .anyMatch(message -> !message.isSuccess());
  }

  /**
   * Get the last failed message that was published.
   */
  public TransformDocumentCompletedEvent getLastFailedMessage() {
    return completedMessages.stream()
        .filter(message -> !message.isSuccess())
        .reduce((first, second) -> second)
        .orElse(null);
  }

  /**
   * Get all failed messages that were published.
   */
  public List<TransformDocumentCompletedEvent> getFailedMessages() {
    return completedMessages.stream()
        .filter(message -> !message.isSuccess())
        .toList();
  }

  /**
   * Get the count of completed messages published.
   */
  public int getCompletedMessageCount() {
    return completedMessages.size();
  }

  /**
   * Find a completed message for a specific document.
   */
  public TransformDocumentCompletedEvent findCompletedMessage(String documentId) {
    return completedMessages.stream()
        .filter(msg -> msg.documentId().equals(documentId))
        .findFirst()
        .orElse(null);
  }

  /**
   * Reset the state of this fake.
   */
  public void reset() {
    completedMessages.clear();
  }
}
