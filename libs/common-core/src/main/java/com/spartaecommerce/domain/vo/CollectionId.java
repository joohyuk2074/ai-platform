package com.spartaecommerce.domain.vo;

import java.util.UUID;

public final class CollectionId extends BaseId<Long> {

  public CollectionId(Long value) {
    super(value);
  }

  /**
   * String 값으로부터 CollectionId를 생성합니다.
   * 주로 HTTP 요청 파라미터나 DTO에서 사용됩니다.
   *
   * @param value String 형태의 ID 값
   * @return CollectionId 인스턴스
   * @throws NumberFormatException value가 Long으로 파싱될 수 없는 경우
   */
  public static CollectionId of(String value) {
    return new CollectionId(Long.parseLong(value));
  }

  /**
   * 테스트용 랜덤 CollectionId를 생성합니다.
   * UUID의 mostSignificantBits를 사용하여 고유한 Long 값을 생성합니다.
   *
   * @return 랜덤 CollectionId 인스턴스
   */
  public static CollectionId generate() {
    return new CollectionId(UUID.randomUUID().getMostSignificantBits());
  }
}
