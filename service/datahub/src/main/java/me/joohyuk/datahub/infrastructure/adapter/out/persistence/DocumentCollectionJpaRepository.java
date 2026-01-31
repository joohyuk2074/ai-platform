package me.joohyuk.datahub.infrastructure.adapter.persistence;

import java.util.Optional;
import me.joohyuk.datahub.infrastructure.adapter.persistence.entity.DocumentCollectionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DocumentCollection의 Spring Data JPA Repository
 */
public interface DocumentCollectionJpaRepository extends JpaRepository<DocumentCollectionJpaEntity, Long> {

  /**
   * 이름으로 컬렉션 조회
   *
   * @param name 컬렉션 이름
   * @return 컬렉션 엔티티
   */
  Optional<DocumentCollectionJpaEntity> findByName(String name);

  /**
   * 이름으로 존재 여부 확인
   *
   * @param name 컬렉션 이름
   * @return 존재 여부
   */
  boolean existsByName(String name);
}
