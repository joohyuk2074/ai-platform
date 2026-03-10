<!--
PR 제목: type(scope): 간결한 제목 (50자 이내)
예시: feat(order): 주문 취소 시 포인트 환불 기능 추가
     fix(payment): 결제 시간 초과 시 재시도 로직 개선
-->

## 📋 변경 사항

### What (무엇을)
<!-- 이 PR에서 변경한 내용을 3줄 이내로 요약 -->


### Why (왜)
<!-- 비즈니스 임팩트 또는 문제 상황 -->
- **배경**:
- **목표**:


### How (어떻게)
<!-- 핵심 구현 방법 (자세한 코드 설명 X, 접근 방식만) -->
-


---

## 🧪 테스트

### 테스트 완료 항목
- [ ] 단위 테스트 작성 및 통과
- [ ] 통합 테스트 작성 및 통과 (Testcontainers 등)
- [ ] 로컬 환경 E2E 테스트 완료
- [ ] 전체 테스트 통과: `./gradlew test`

### 테스트 시나리오
<!-- 핵심 테스트 케이스 (선택사항) -->
1. 정상 케이스:
2. 예외 케이스:


---

## 🚀 배포 영향도

### 영향 받는 서비스/모듈
- [ ] `service/datahub`
- [ ] `service/datarex`
- [ ] `service/vecdash`
- [ ] `libs/common-*`

### API 변경 사항
- [ ] 신규 API 추가
- [ ] 기존 API 수정 (Breaking Change 여부: Yes / No)
- [ ] 기존 API 삭제 (Deprecated 처리 여부: Yes / No)

### DB 스키마 변경
- [ ] 마이그레이션 스크립트 작성 완료 (`schema/V*.sql`)
- [ ] 롤백 스크립트 작성 완료
- [ ] 인덱스 추가/변경으로 인한 성능 영향 검토

### 외부 의존성 변경
- [ ] Kafka 토픽 추가/변경
- [ ] 새로운 외부 API 호출 추가
- [ ] 새로운 라이브러리/의존성 추가


---

## 🔍 리스크 및 대응

### 예상 리스크
<!-- 이 변경으로 발생할 수 있는 문제 -->
- [ ] 성능 저하 가능성 (예상 응답 시간: )
- [ ] 데이터 정합성 이슈
- [ ] 서비스 간 의존성 증가
- [ ] 리스크 없음

### 롤백 계획
<!-- 문제 발생 시 대응 방법 -->
-


---

## 📊 성능 및 모니터링

### 성능 영향
- [ ] 성능 테스트 완료 (JMeter, Gatling 등)
- [ ] 쿼리 성능 검증 (EXPLAIN, 실행 계획 확인)
- [ ] 캐시 전략 적용

### 모니터링 지표
<!-- 배포 후 확인할 메트릭 -->
- [ ] 로그 추가 (레벨: INFO / WARN / ERROR)
- [ ] 메트릭 추가 (Prometheus, Micrometer)
- [ ] 알람 설정 필요 여부: Yes / No


---

## ✅ 체크리스트

### 코드 품질
- [ ] 불필요한 주석 제거
- [ ] 하드코딩된 값 제거 (환경 변수 또는 설정 파일 사용)
- [ ] 예외 처리 적절히 구현
- [ ] 로깅 레벨 적절히 설정
- [ ] NULL 체크 및 방어 코드 작성

### 보안
- [ ] 민감 정보 하드코딩 없음 (API 키, 비밀번호 등)
- [ ] SQL Injection 대응 (PreparedStatement, JPA 사용)
- [ ] XSS 대응 (입력값 검증/이스케이프)
- [ ] 인증/인가 로직 적절히 구현
- [ ] 개인정보 처리 로직 검토 완료

### 문서
- [ ] API 명세 업데이트 (Swagger/OpenAPI)
- [ ] README 또는 기술 문서 업데이트
- [ ] 설정 변경 사항 문서화 (`application.yml` 등)


---

## 🖼️ 스크린샷 / 데모

<!-- UI 변경이 있는 경우 Before/After 스크린샷 첨부 -->
<!-- API 변경인 경우 Postman/Curl 예제 첨부 -->

<details>
<summary>API 호출 예제</summary>

```bash
# Request
curl -X POST http://localhost:8082/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Sample Document",
    "content": "This is a test"
  }'

# Response
{
  "id": "doc-12345",
  "status": "CREATED",
  "createdAt": "2026-03-11T10:00:00Z"
}
```

</details>


---

## 💬 리뷰어에게

<!-- 특별히 검토가 필요한 부분, 고민했던 부분, 대안에 대한 의견 요청 등 -->
-


---

## 🔗 관련 이슈

Closes #
Related to #
