package me.joohyuk.datahub.domain.port.in.listener;

import com.spartaecommerce.domain.vo.DocumentId;

/**
 * 인덱싱 응답 메시지
 *
 * 인덱싱 처리 결과를 담는 DTO입니다.
 * TODO: 실제 필요한 필드들로 수정 필요
 */
public record IndexingResponse(
    DocumentId documentId,
    boolean success,
    String message
) {
}
