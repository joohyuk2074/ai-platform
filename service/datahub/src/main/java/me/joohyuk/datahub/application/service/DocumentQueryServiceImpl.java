package me.joohyuk.datahub.application.service;

import java.io.InputStream;
import me.joohyuk.datahub.application.port.in.service.DocumentQueryService;
import com.spartaecommerce.domain.vo.DocumentId;

public class DocumentQueryServiceImpl implements DocumentQueryService {

  /**
   * 문서 파일을 다운로드합니다.
   *
   * @param documentId 다운로드할 문서 ID
   * @return 파일 입력 스트림
   */
  @Override
  public InputStream downloadDocument(DocumentId documentId) {
    // TODO: 구현
    // 1. Document 조회
    // 2. fileKey로 파일 저장소에서 파일 읽기
    // 3. InputStream 반환
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
