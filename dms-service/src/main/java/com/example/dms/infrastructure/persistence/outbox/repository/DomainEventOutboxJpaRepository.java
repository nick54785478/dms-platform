package com.example.dms.infrastructure.persistence.outbox.repository;

import com.example.dms.infrastructure.persistence.outbox.entity.DomainEventOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DomainEventOutboxJpaRepository extends JpaRepository<DomainEventOutboxJpaEntity, UUID> {
    
    /**
     * 尋找特定狀態的事件 (例如尋找 PENDING 準備發送的事件)
     * 在正式環境中，如果要處理並行，可能需要用 @Lock 或是更複雜的機制
     */
    List<DomainEventOutboxJpaEntity> findByStatusOrderByCreatedAtAsc(DomainEventOutboxJpaEntity.OutboxStatus status);
}
