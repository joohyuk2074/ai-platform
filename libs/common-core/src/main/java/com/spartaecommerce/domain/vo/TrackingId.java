package com.spartaecommerce.domain.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;

public class TrackingId extends BaseId<UUID> {

  public TrackingId(UUID value) {
    super(value);
  }

  @JsonCreator
  public static TrackingId of(String trackingId) {
    if (trackingId == null) {
      throw new IllegalArgumentException("trackingId cannot be null");
    }
    return new TrackingId(UUID.fromString(trackingId));
  }
}
