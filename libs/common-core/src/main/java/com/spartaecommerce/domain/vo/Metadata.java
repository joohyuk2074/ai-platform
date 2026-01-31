package com.spartaecommerce.domain.vo;

import java.util.ArrayList;
import java.util.List;

public record Metadata(
    String fileName,      // 원본 파일명
    Long fileSize,       // 파일 크기 (바이트)
    String contentType,  // MIME 타입 (예: "text/markdown", "application/pdf")
    UserId uploadedBy,   // 업로드한 사용자 ID
    String source,       // 문서 출처
    String author,       // 문서 작성자
    List<String> tags    // 태그 목록
) {

  /**
   * 기본 메타데이터 생성 (파일 업로드용)
   */
  public static Metadata of(
      String fileName,
      Long fileSize,
      String contentType,
      Long uploadedBy
  ) {
    return new Metadata(
        fileName,
        fileSize,
        contentType,
        new UserId(uploadedBy),
        null,
        null,
        new ArrayList<>()
    );
  }

  /**
   * 두 메타데이터를 병합합니다. base 메타데이터에 additional 메타데이터의 값을 추가합니다.
   */
  public static Metadata merge(Metadata base, Metadata additional) {
    if (base == null) {
      return additional;
    }
    if (additional == null) {
      return base;
    }

    List<String> mergedTags = new ArrayList<>();
    if (base.tags != null) {
      mergedTags.addAll(base.tags);
    }
    if (additional.tags != null) {
      additional.tags.stream()
          .filter(tag -> !mergedTags.contains(tag))
          .forEach(mergedTags::add);
    }

    return new Metadata(
        additional.fileName != null ? additional.fileName : base.fileName,
        additional.fileSize != null ? additional.fileSize : base.fileSize,
        additional.contentType != null ? additional.contentType : base.contentType,
        additional.uploadedBy != null ? additional.uploadedBy : base.uploadedBy,
        additional.source != null ? additional.source : base.source,
        additional.author != null ? additional.author : base.author,
        mergedTags
    );
  }
}
