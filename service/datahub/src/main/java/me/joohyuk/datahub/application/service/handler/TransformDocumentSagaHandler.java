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
      case TRANSFORMED -> SagaStatus.PROCESSING;  // Transform 완료, Embed 단계 남음 (Saga 진행 중)
      case TRANSFORM_FAILED -> SagaStatus.FAILED;  // Transform 실패
      case TRANSFORM_COMPENSATING -> SagaStatus.COMPENSATING;  // 보상 트랜잭션 진행 중
      case TRANSFORM_COMPENSATED -> SagaStatus.COMPENSATED;  // 보상 완료, 롤백됨

      case EMBED_REQUESTED -> SagaStatus.PROCESSING;   // Embed 요청 완료 (Saga 진행 중)
      case EMBEDDING -> SagaStatus.PROCESSING;         // 실제 Embed 작업 진행 중
      case EMBEDDED -> SagaStatus.SUCCEEDED;           // Embed 완료, Saga 전체 성공 (terminal)
      case EMBED_FAILED -> SagaStatus.FAILED;          // Embed 실패
      case EMBED_COMPENSATING -> SagaStatus.COMPENSATING;  // Embed 보상 트랜잭션 진행 중
      case EMBED_COMPENSATED -> SagaStatus.COMPENSATED;    // Embed 보상 완료, 롤백됨
    };
  }

  public SagaStatus documentStatusNameToSagaStatus(String documentStatusName) {
    DocumentStatus documentStatus = DocumentStatus.valueOf(documentStatusName);
    return documentStatusToSagaStatus(documentStatus);
  }
}
