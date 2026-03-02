# Datahub API Postman Collection

Datahub 서비스의 REST API를 테스트하기 위한 Postman 컬렉션입니다.

## 파일 구성

- `Datahub_API_Collection.postman_collection.json`: API 엔드포인트 컬렉션
- `Datahub_API.postman_environment.json`: 환경 변수 설정

## Import 방법

### 1. Postman 실행

Postman 애플리케이션을 실행합니다.

### 2. Collection Import

1. Postman 좌측 상단의 **Import** 버튼 클릭
2. **Upload Files** 선택
3. `Datahub_API_Collection.postman_collection.json` 파일 선택
4. **Import** 버튼 클릭

### 3. Environment Import

1. Postman 좌측 상단의 **Import** 버튼 클릭
2. **Upload Files** 선택
3. `Datahub_API.postman_environment.json` 파일 선택
4. **Import** 버튼 클릭

### 4. Environment 활성화

1. Postman 우측 상단의 Environment 드롭다운 클릭
2. **Datahub API Environment** 선택

## API 엔드포인트

### Collection APIs

#### 1. Create Collection
- **Method**: POST
- **URL**: `/api/v1/collections`
- **Description**: 새로운 문서 컬렉션을 생성합니다.
- **Request Body**:
  ```json
  {
    "name": "AI Research Papers",
    "description": "인공지능 관련 연구 논문 모음",
    "source": "arXiv",
    "author": "Various Authors",
    "tags": ["AI", "Machine Learning", "Deep Learning"],
    "createdBy": {
      "value": 1
    }
  }
  ```
- **Response**: 생성된 컬렉션 ID 반환 (자동으로 환경 변수에 저장됨)

#### 2. Update Collection
- **Method**: PUT
- **URL**: `/api/v1/collections/{collectionId}`
- **Description**: 기존 컬렉션의 정보를 수정합니다.
- **Request Body**:
  ```json
  {
    "name": "AI Research Papers (Updated)",
    "description": "인공지능 관련 연구 논문 모음 - 2024년 최신 논문 추가"
  }
  ```

#### 3. Delete Collection
- **Method**: DELETE
- **URL**: `/api/v1/collections/{collectionId}`
- **Description**: 컬렉션을 삭제합니다.

### Document APIs

#### 1. Upload Document
- **Method**: POST
- **URL**: `/api/v1/documents/upload`
- **Content-Type**: multipart/form-data
- **Description**: 문서 파일을 업로드합니다.
- **Form Data**:
  - `file`: 업로드할 파일 (필수)
  - `collectionId`: 문서를 저장할 컬렉션 ID (필수)

## 인증 (X-Passport Header)

모든 API 요청에는 `X-Passport` 헤더가 필요합니다. 이는 Netflix Passport 패턴을 따르는 인증 방식입니다.

### X-Passport 헤더 형식

```json
{
  "userId": {
    "value": 1
  },
  "username": "testuser",
  "roles": ["USER"]
}
```

컬렉션에는 기본값이 설정되어 있으며, 필요시 수정할 수 있습니다.

## 환경 변수

| 변수명 | 기본값 | 설명 |
|-------|--------|------|
| `baseUrl` | http://localhost:8080 | API 서버 기본 URL |
| `collectionId` | 1 | 테스트용 컬렉션 ID (Create Collection 성공 시 자동 업데이트) |
| `userId` | 1 | 테스트 사용자 ID |
| `username` | testuser | 테스트 사용자 이름 |

## 사용 순서

1. **Create Collection** 실행
   - 새로운 컬렉션을 생성합니다.
   - 응답에서 받은 `collectionId`가 자동으로 환경 변수에 저장됩니다.

2. **Upload Document** 실행
   - 파일 선택: form-data의 `file` 필드에서 업로드할 파일을 선택합니다.
   - 생성된 컬렉션에 문서가 업로드됩니다.

3. **Update Collection** 실행 (선택)
   - 컬렉션 정보를 수정합니다.

4. **Delete Collection** 실행 (선택)
   - 컬렉션을 삭제합니다.

## 테스트 스크립트

각 요청에는 자동 테스트 스크립트가 포함되어 있습니다:

- **Create Collection**:
  - 201 상태 코드 확인
  - 응답 구조 검증
  - collectionId 자동 저장

- **Upload Document**:
  - 201 상태 코드 확인
  - documentId 반환 확인

## 문제 해결

### 포트 변경이 필요한 경우
Environment의 `baseUrl` 변수를 수정하세요.

### 다른 사용자로 테스트하고 싶은 경우
각 요청의 `X-Passport` 헤더 값을 수정하세요.

### 파일 업로드가 실패하는 경우
1. 파일 크기 확인 (Spring Boot의 기본 제한: 1MB)
2. 파일 형식 확인
3. collectionId가 존재하는지 확인

## 참고 사항

- 헥사고날 아키텍처를 따르는 API 구조
- 모든 응답은 `CommonResponse` 래퍼로 감싸져 있습니다:
  ```json
  {
    "success": true,
    "data": { ... }
  }
  ```
