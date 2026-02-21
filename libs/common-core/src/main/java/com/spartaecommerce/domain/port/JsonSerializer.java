package com.spartaecommerce.domain.port;

/**
 * JSON 직렬화/역직렬화를 위한 Port 인터페이스
 * <p>
 * 헥사고날 아키텍처의 Port로서, 도메인 계층이 특정 JSON 라이브러리에 의존하지 않도록 추상화를 제공합니다.
 * Jackson, Gson 등 다양한 구현체로 교체 가능합니다.
 */
public interface JsonSerializer {

  /**
   * 객체를 JSON 문자열로 직렬화합니다.
   *
   * @param object 직렬화할 객체
   * @param <T>    객체 타입
   * @return JSON 문자열
   * @throws JsonSerializationException 직렬화 실패 시
   */
  <T> String serialize(T object);

  /**
   * JSON 문자열을 지정된 타입의 객체로 역직렬화합니다.
   *
   * @param json  JSON 문자열
   * @param clazz 대상 클래스 타입
   * @param <T>   반환 타입
   * @return 역직렬화된 객체
   * @throws JsonSerializationException 역직렬화 실패 시
   */
  <T> T deserialize(String json, Class<T> clazz);
}
