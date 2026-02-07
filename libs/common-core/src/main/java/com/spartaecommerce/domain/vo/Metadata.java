package com.spartaecommerce.domain.vo;

import java.util.ArrayList;
import java.util.List;

public record Metadata(
    String fileName,      // 원본 파일명
    Long fileSize,       // 파일 크기 (바이트)
    String contentType,  // MIME 타입 (예: "text/markdown", "application/pdf")
    Long uploadedBy,   // 업로드한 사용자 ID
    String source,       // 문서 출처
    String author,       // 문서 작성자
    List<String> tags    // 태그 목록
) {

  public static Metadata forUpload(
      String fileName,
      Long fileSize,
      String contentType,
      Long uploadedBy
  ) {
    return new Metadata(
        fileName,
        fileSize,
        contentType,
        uploadedBy,
        null,
        null,
        new ArrayList<>()
    );
  }
}
