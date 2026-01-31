package me.joohyuk.datahub.application.dto.request;

public record UpdateDocumentCollectionCommand(
    String name,
    String description
) {

  public UpdateDocumentCollectionCommand {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Collection name cannot be empty");
    }
    // description은 nullable 허용
  }
}
