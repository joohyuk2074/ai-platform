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

  /**
   * 콘텐츠 해시로 존재 여부 확인 (중복 파일 검증용)
   *
   * @param contentHash SHA-256 콘텐츠 해시값
   * @return 존재 여부
   */
  boolean existsByContentHash(String contentHash);

  /**
   * 컬렉션 ID로 해당 컬렉션의 문서 목록 조회
   *
   * @param collectionId 컬렉션 ID
   * @return 문서 엔티티 리스트
   */
  List<DocumentJpaEntity> findByCollectionId(Long collectionId);
}
