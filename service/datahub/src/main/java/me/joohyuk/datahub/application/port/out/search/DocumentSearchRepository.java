package me.joohyuk.datahub.application.port.out.search;

import java.util.List;
import me.joohyuk.datahub.domain.entity.Document;

public interface DocumentSearchRepository {

  List<Document> searchByKeyword(String keyword, int page, int size);

  List<Document> searchByContent(String content);

  long countByKeyword(String keyword);
}
