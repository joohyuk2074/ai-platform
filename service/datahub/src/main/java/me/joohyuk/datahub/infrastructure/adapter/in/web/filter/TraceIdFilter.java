package me.joohyuk.datahub.infrastructure.adapter.in.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 분산 추적을 위한 Trace ID 필터
 *
 * <p>모든 HTTP 요청에 대해 고유한 traceId를 생성하고 MDC에 설정합니다.
 * 이는 다음과 같은 이점을 제공합니다:
 * <ul>
 *   <li>로그 추적: 단일 요청과 관련된 모든 로그를 traceId로 추적</li>
 *   <li>분산 추적: 마이크로서비스 간 요청 흐름 추적 (Zipkin, Jaeger 등과 연동 가능)</li>
 *   <li>디버깅: 특정 요청의 전체 라이프사이클 분석</li>
 *   <li>에러 추적: 에러 발생 시 traceId를 통해 관련 로그 검색</li>
 * </ul>
 *
 * <p>클라이언트가 {@code X-Trace-Id} 헤더를 제공하면 해당 값을 사용하고,
 * 없으면 새로운 UUID를 생성합니다. (분산 추적 시스템과의 호환성)
 *
 * <p>빅테크 기업(Google, Amazon, Netflix 등)에서 널리 사용하는 패턴입니다.
 */
@Component
@Order(1) // 가장 먼저 실행되어야 함
public class TraceIdFilter extends OncePerRequestFilter {

  private static final String TRACE_ID_HEADER = "X-Trace-Id";
  private static final String TRACE_ID_KEY = "traceId";
  private static final String SPAN_ID_KEY = "spanId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      // 클라이언트가 제공한 traceId 확인 (분산 추적 지원)
      String traceId = request.getHeader(TRACE_ID_HEADER);
      if (traceId == null || traceId.isBlank()) {
        // traceId가 없으면 새로 생성
        traceId = UUID.randomUUID().toString();
      }

      // 현재 요청의 고유 spanId 생성 (분산 추적에서 사용)
      String spanId = UUID.randomUUID().toString();

      // MDC에 설정 (로그에서 사용)
      MDC.put(TRACE_ID_KEY, traceId);
      MDC.put(SPAN_ID_KEY, spanId);

      // 응답 헤더에 traceId 추가 (클라이언트가 에러 발생 시 traceId를 서버에 전달할 수 있음)
      response.setHeader(TRACE_ID_HEADER, traceId);

      filterChain.doFilter(request, response);
    } finally {
      // 요청 처리 완료 후 MDC 정리 (메모리 누수 방지)
      MDC.clear();
    }
  }
}
