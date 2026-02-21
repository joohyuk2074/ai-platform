package me.joohyuk.datahub.infrastructure.adapter.web.auth;

import com.spartaecommerce.domain.entity.Passport;
import com.spartaecommerce.domain.port.JsonSerializer;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Netflix Passport 패턴 구현: API Gateway에서 X-Passport 헤더로 전달된 JSON을 파싱하여 Controller 메서드에 Passport 객체로
 * 주입
 * <p>
 * 헤더 형식: - Plain JSON: X-Passport: {"userId":123,"username":"john","roles":["USER"]} - Base64 (선택):
 * X-Passport: eyJ1c2VySWQiOjEyMywidXNlcm5hbWUiOiJqb2huIiwicm9sZXMiOlsiVVNFUiJdfQ==
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PassportArgumentResolver implements HandlerMethodArgumentResolver {

  private static final String PASSPORT_HEADER = "X-Passport";

  private final JsonSerializer jsonSerializer;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(
        me.joohyuk.datahub.infrastructure.adapter.web.auth.AuthenticatedUser.class)
        && parameter.getParameterType().equals(Passport.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory
  ) throws Exception {

    HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

    if (request == null) {
      throw new IllegalStateException("HttpServletRequest is null");
    }

    String passportHeader = request.getHeader(PASSPORT_HEADER);

    if (passportHeader == null || passportHeader.isBlank()) {
      log.error("Missing {} header in request to {}", PASSPORT_HEADER, request.getRequestURI());
      throw new me.joohyuk.datahub.infrastructure.adapter.web.auth.UnauthorizedException(
          "User authentication required");
    }

    try {
      // Base64 인코딩 여부 확인 (선택적)
      String jsonString = isBase64Encoded(passportHeader)
          ? decodeBase64(passportHeader)
          : passportHeader;

      // JSON을 Passport 객체로 역직렬화
      Passport passport = jsonSerializer.deserialize(jsonString, Passport.class);

      log.debug("Resolved Passport - userId: {}, username: {}, roles: {}",
          passport.userId().getValue(), passport.username(), passport.roles());

      return passport;

    } catch (Exception e) {
      log.error("Failed to parse {} header: {}", PASSPORT_HEADER, passportHeader, e);
      throw new me.joohyuk.datahub.infrastructure.adapter.web.auth.UnauthorizedException(
          "Invalid passport format");
    }
  }

  /**
   * Base64 인코딩 여부 확인 '{' 문자로 시작하면 Plain JSON, 아니면 Base64로 간주
   */
  private boolean isBase64Encoded(String value) {
    return !value.trim().startsWith("{");
  }

  /**
   * Base64 디코딩
   */
  private String decodeBase64(String encoded) {
    try {
      byte[] decodedBytes = Base64.getDecoder().decode(encoded);
      return new String(decodedBytes);
    } catch (IllegalArgumentException e) {
      log.warn("Failed to decode Base64 passport, treating as plain JSON: {}", encoded);
      return encoded;
    }
  }
}
