package project.green.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import project.green.entity.Offset;
import project.green.entity.OffsetId;
import reactor.core.publisher.Mono;

@Repository
public interface OffsetRepository extends ReactiveCrudRepository<Offset, OffsetId> {
    @Modifying
    @Query("INSERT INTO offset_table (account, transaction_id) VALUES (:account, :transactionId) ON CONFLICT (account) DO UPDATE SET transaction_id = EXCLUDED.transaction_id")
    Mono<Void> save(String account, Long transactionId);
}
