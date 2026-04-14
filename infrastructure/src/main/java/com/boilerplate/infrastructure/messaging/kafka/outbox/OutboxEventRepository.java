package com.boilerplate.infrastructure.messaging.kafka.outbox;

import com.boilerplate.infrastructure.messaging.kafka.outbox.OutboxEventEntity.Status;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Spring Data repository for outbox events within a tenant schema. */
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

  @Query("SELECT o FROM OutboxEventEntity o WHERE o.status = 'PENDING' ORDER BY o.createdAt ASC")
  List<OutboxEventEntity> findPending();
}
