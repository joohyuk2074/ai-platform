# Sample Document for Testing

이 문서는 Datahub API의 파일 업로드 기능을 테스트하기 위한 샘플 문서입니다.

## 문서 정보

- **제목**: 인공지능과 머신러닝 개요
- **작성일**: 2024년 1월
- **카테고리**: AI/ML

## 개요

인공지능(Artificial Intelligence, AI)은 인간의 학습능력, 추론능력, 지각능력을 인공적으로 구현하려는 컴퓨터 과학의 세부분야입니다.

## 주요 개념

### 1. 머신러닝 (Machine Learning)

머신러닝은 컴퓨터가 명시적으로 프로그래밍되지 않고도 학습할 수 있는 능력을 제공하는 AI의 한 분야입니다.

**주요 알고리즘:**
- 선형 회귀 (Linear Regression)
- 로지스틱 회귀 (Logistic Regression)
- 결정 트리 (Decision Tree)
- 랜덤 포레스트 (Random Forest)
- 서포트 벡터 머신 (SVM)

### 2. 딥러닝 (Deep Learning)

딥러닝은 인공신경망을 기반으로 한 머신러닝의 한 분야입니다.

**주요 아키텍처:**
- CNN (Convolutional Neural Network)
- RNN (Recurrent Neural Network)
- LSTM (Long Short-Term Memory)
- Transformer
- GAN (Generative Adversarial Network)

### 3. 자연어 처리 (NLP)

자연어 처리는 인간의 언어를 컴퓨터가 이해하고 처리할 수 있도록 하는 기술입니다.

**주요 작업:**
- 텍스트 분류
- 감성 분석
- 기계 번역
- 질의응답 시스템
- 텍스트 생성

## 응용 분야

1. **이미지 인식**
   - 얼굴 인식
   - 객체 탐지
   - 의료 영상 분석

2. **음성 인식**
   - 음성 비서 (Siri, Alexa)
   - 실시간 자막 생성

3. **자율 주행**
   - 차선 인식
   - 장애물 회피
   - 경로 계획

4. **추천 시스템**
   - 콘텐츠 추천
   - 상품 추천
   - 맞춤형 광고

## 코드 예시

```python
# 간단한 선형 회귀 예제
import numpy as np
from sklearn.linear_model import LinearRegression

# 데이터 준비
X = np.array([[1], [2], [3], [4], [5]])
y = np.array([2, 4, 6, 8, 10])

# 모델 학습
model = LinearRegression()
model.fit(X, y)

# 예측
prediction = model.predict([[6]])
print(f"예측 결과: {prediction[0]}")
```

## 참고 자료

- [Stanford CS229: Machine Learning](https://cs229.stanford.edu/)
- [Deep Learning Specialization by Andrew Ng](https://www.coursera.org/specializations/deep-learning)
- [Papers with Code](https://paperswithcode.com/)

## 결론

인공지능과 머신러닝 기술은 계속해서 발전하고 있으며, 다양한 산업 분야에서 혁신을 이끌고 있습니다. 이 문서는 테스트 목적으로 작성되었으며, 실제 Datahub 시스템에서 문서 업로드 및 처리 기능을 검증하는 데 사용됩니다.

---

**테스트 메타데이터:**
- 파일 크기: ~3KB
- 형식: Markdown
- 인코딩: UTF-8
- 언어: 한국어/영어 혼합
