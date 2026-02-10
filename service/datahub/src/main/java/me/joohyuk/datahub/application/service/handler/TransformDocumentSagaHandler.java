package me.joohyuk.datahub.application.service.handler;

import lombok.extern.slf4j.Slf4j;
import me.joohyuk.commonsaga.SagaStatus;
import me.joohyuk.datahub.domain.vo.DocumentStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransformDocumentSagaHandler {

  public SagaStatus documentStatusToSagaStatus(DocumentStatus documentStatus) {
    return switch (documentStatus) {
      case UPLOADED -> SagaStatus.STARTED;  // Saga 시작 전 초기 상태
      case TRANSFORM_REQUESTED -> SagaStatus.STARTED;  // Saga 시작됨, 작업 요청 완료
      case TRANSFORMING -> SagaStatus.PROCESSING;  // 실제 Transform 작업 진행 중
      case TRANSFORMED -> SagaStatus.SUCCEEDED;  // Transform 성공적으로 완료
      case TRANSFORM_FAILED -> SagaStatus.FAILED;  // Transform 실패
      case TRANSFORM_COMPENSATING -> SagaStatus.COMPENSATING;  // 보상 트랜잭션 진행 중
      case TRANSFORM_COMPENSATED -> SagaStatus.COMPENSATED;  // 보상 완료, 롤백됨
    };
  }
}
