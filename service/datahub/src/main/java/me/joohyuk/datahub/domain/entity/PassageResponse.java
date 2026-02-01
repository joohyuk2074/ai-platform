package me.joohyuk.datahub.domain.entity;

import java.util.List;
import lombok.Getter;

@Getter
public class PassageResponse {

  private Long documentId;

  private List<String> failureMessages;

  // TODO: 네이밍 수정 및 적절한 위치이동 필요
}
