package me.joohyuk.datahub.application.dto.command;

public record CreateDocumentCollectionCommand(
    String name,
    String description
//    String source,        // 문서 출처
//    String author,        // 작성자
//    List<String> tags,    // 태그 목록
//    UserId createdBy        // 생성한 사용자 ID
) {

  public CreateDocumentCollectionCommand {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Collection name cannot be empty");
    }
//    // description은 nullable 허용
//    // tags가 null이면 빈 리스트로 초기화
//    tags = tags == null ? List.of() : tags;
  }
}
