package me.joohyuk.datahub.infrastructure.config;

import lombok.RequiredArgsConstructor;
import me.joohyuk.datahub.infrastructure.adapter.web.auth.PassportArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 설정
 * Netflix Passport 패턴을 위한 ArgumentResolver 등록
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final PassportArgumentResolver passportArgumentResolver;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(passportArgumentResolver);
  }
}
