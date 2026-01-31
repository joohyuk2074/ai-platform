package me.joohyuk.datahub.infrastructure.adapter.out.persistence;

import java.util.List;
import me.joohyuk.datahub.infrastructure.adapter.out.persistence.entity.DocumentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Document의 Spring Data JPA Repository
 */
public interface DocumentJpaRepository extends JpaRepository<DocumentJpaEntity, Long> {

  /**
   * 파일 키로 문서 조회
   *
   * @param fileKey 파일 키
   * @return 문서 엔티티 리스트
   */
  List<DocumentJpaEntity> findByFileKey(String fileKey);

  /**
   * 파일 키로 존재 여부 확인
   *
   * @param fileKey 파일 키
   * @return 존재 여부
   */
  boolean existsByFileKey(String fileKey);
}
