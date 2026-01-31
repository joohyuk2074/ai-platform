package me.joohyuk.datahub.infrastructure.adapter.web.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller 메서드 파라미터에 Passport 객체를 주입하기 위한 어노테이션
 *
 * Netflix Passport 패턴:
 * API Gateway가 X-Passport 헤더로 전달한 JSON을 자동으로 파싱
 *
 * 사용 예시:
 * <pre>
 * {@code
 * @PostMapping
 * public ResponseEntity<?> create(
 *     @AuthenticatedUser Passport passport,
 *     @RequestBody SomeCommand command
 * ) {
 *     String userId = passport.userId().value();
 *     ...
 * }
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticatedUser {
}
