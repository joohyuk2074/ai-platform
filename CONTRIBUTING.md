# Contributing to AI Platform

이 프로젝트에 기여해주셔서 감사합니다! 이 문서는 협업 규칙과 워크플로우를 설명합니다.

## 1. Repository

- **GitHub Repository:** `joohyuk2074/ai-platform`
- **Main Branch:** `main`

## 2. Branching Strategy

### 브랜치 네이밍 규칙

모든 기능 개발과 수정은 목적에 맞는 브랜치에서 진행합니다:

- **기능 개발**: `feature/[이슈번호]-[간단-설명-kebab-case]`
  - 예시: `feature/42-add-document-versioning`
- **버그 수정**: `fix/[이슈번호]-[간단-설명]`
  - 예시: `fix/15-null-pointer-in-transformer`
- **잡무/문서**: `chore/[간단-설명]`
  - 예시: `chore/update-readme`

### 브랜치 생성 절차

1. 최신 main 브랜치에서 시작:
   ```bash
   git checkout main
   git pull origin main
   ```

2. 새 브랜치 생성:
   ```bash
   git checkout -b feature/123-your-feature
   ```

## 3. Commit Message Convention

### Conventional Commits 명세 준수

모든 커밋 메시지는 [Conventional Commits](https://www.conventionalcommits.org/) 형식을 따릅니다:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 커밋 타입

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅 (기능 변경 없음)
- `refactor`: 코드 리팩토링 (기능 변경 없음)
- `test`: 테스트 추가/수정
- `chore`: 빌드/설정 변경

### 예시

```
feat(datahub): Add document versioning support

Implement version tracking for documents using event sourcing.
Each document now maintains a version history with snapshots.

Closes #42
```

```
fix(datarex): Prevent null pointer in markdown transformer

Add null check before accessing transformation result.
This fixes the NPE when processing empty documents.

Closes #15
```

### 필수 사항

- **Subject**: 50자 이내, 명령형 현재 시제 (Add, Fix, Update)
- **Body**: 변경 이유를 명확히 설명
- **Footer**: 관련 이슈를 `Closes #[이슈번호]` 형식으로 반드시 포함
- 절대 claude 관련된 커밋 내용을 남기지 말고 author에도 적지마세요.

## 4. Pull Request (PR) Process

### PR 생성 전 체크리스트

- [ ] 코드가 프로젝트의 헥사고날 아키텍처 패턴을 따르는가?
- [ ] 모든 테스트가 통과하는가? (`./gradlew test`)
- [ ] 빌드가 성공하는가? (`./gradlew build`)
- [ ] 커밋 메시지가 Conventional Commits를 따르는가?
- [ ] 코드에 불필요한 주석이 없는가?

### PR 제출 규칙

1. **직접 푸시 금지**: `main` 브랜치로 직접 푸시할 수 없습니다.
2. **코드 리뷰 필수**: 모든 PR은 최소 1명의 리뷰어 승인이 필요합니다.
3. **PR 제목**: 커밋 메시지와 동일한 형식
   - 예시: `feat(datahub): Add document versioning support`
4. **PR 본문**: [`.github/pull_request_template.md`](./.github/pull_request_template.md) 템플릿 자동 사용

### PR 템플릿 주요 섹션

PR 생성 시 자동으로 제공되는 템플릿에는 다음 섹션이 포함됩니다:

- **변경 사항**: What/Why/How 형식으로 변경 내용 설명
- **테스트**: 단위/통합/E2E 테스트 완료 여부, 테스트 시나리오
- **배포 영향도**: 영향 받는 서비스, API 변경, DB 스키마, 외부 의존성
- **리스크 및 대응**: 예상 리스크, 롤백 계획
- **성능 및 모니터링**: 성능 테스트, 모니터링 지표, 로그/메트릭
- **체크리스트**: 코드 품질, 보안, 문서화
- **스크린샷/데모**: API 호출 예제, UI 변경 화면

상세한 템플릿 형식은 [`.github/pull_request_template.md`](./.github/pull_request_template.md)를 참조하세요.

### PR 리뷰 프로세스

1. **자동 체크**: CI/CD 파이프라인이 빌드와 테스트를 실행
2. **코드 리뷰**: 리뷰어가 코드 품질, 아키텍처 준수, 테스트 커버리지 확인
3. **피드백 반영**: 요청된 변경사항 수정
4. **병합**: 승인 후 squash merge 또는 rebase merge

## 5. 코드 스타일 가이드

### Java 코딩 규칙

- **네이밍**: camelCase (메서드, 변수), PascalCase (클래스)
- **들여쓰기**: 2 spaces
- **줄 길이**: 최대 120자
- **주석**: 복잡한 비즈니스 로직만 주석 작성 (과도한 주석 지양)

### 아키텍처 준수

- **도메인 레이어**: 외부 의존성 절대 금지
- **포트**: 인터페이스로 추상화
- **어댑터**: 구현 세부사항 캡슐화

자세한 아키텍처 가이드는 [CLAUDE.md](./CLAUDE.md)를 참조하세요.

## 6. 이슈 관리

### 이슈 생성

- 버그 리포트, 기능 제안, 질문은 GitHub Issues 사용
- 적절한 라벨 선택 (bug, enhancement, question 등)
- 명확한 제목과 재현 방법 또는 요구사항 작성

### 이슈 할당

- 작업을 시작하기 전에 자신에게 이슈 할당
- 중복 작업 방지를 위해 이슈에 진행 상황 코멘트

## 7. 질문 및 지원

- **기술 문서**: [CLAUDE.md](./CLAUDE.md)
- **아키�ecture**: 헥사고날 패턴, DDD, 이벤트 소싱
- **질문**: GitHub Issues 또는 Discussion 사용

감사합니다! 🚀
