package me.joohyuk.datahub.domain.entity;

import com.spartaecommerce.domain.entity.AggregateRoot;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.exception.DatahubDomainErrorCode;
import com.spartaecommerce.domain.vo.CollectionId;

@Getter
public class DocumentCollection extends AggregateRoot<CollectionId> {

  private String name;
  private String description;
  private final Instant createdAt;
  private Instant updatedAt;

  private DocumentCollection(
      CollectionId id,
      String name,
      String description,
      Instant createdAt,
      Instant updatedAt
  ) {
    super.setId(Objects.requireNonNull(id, "Collection ID cannot be null"));

    if (name == null || name.isBlank()) {
      throw new DatahubDomainException(
          "Collection name cannot be null or empty",
          DatahubDomainErrorCode.INVALID_COLLECTION_NAME
      );
    }
    this.name = name;
    this.description = description;
    this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
    this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
  }

  public static DocumentCollection of(
      Long id,
      String name,
      String description,
      Instant createdAt,
      Instant updatedAt
  ) {
    return new DocumentCollection(
        new CollectionId(id),
        name,
        description,
        createdAt,
        updatedAt
    );
  }

  public void modify(String name, String description, Instant updatedAt) {
    if (name == null || name.isBlank()) {
      throw new DatahubDomainException(
          "Collection name cannot be null or empty",
          DatahubDomainErrorCode.INVALID_COLLECTION_NAME
      );
    }

    this.name = name;
    this.description = description;
    this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
  }
}
