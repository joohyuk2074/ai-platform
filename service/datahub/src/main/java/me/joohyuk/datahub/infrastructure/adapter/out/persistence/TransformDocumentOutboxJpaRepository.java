package me.joohyuk.datahub.infrastructure.adapter.out.persistence;

import me.joohyuk.datahub.infrastructure.adapter.out.persistence.entity.TransformDocumentOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransformDocumentOutboxJpaRepository
    extends JpaRepository<TransformDocumentOutboxJpaEntity, Long> {

}
