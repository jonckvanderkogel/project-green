package project.green.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import project.green.entity.PaymentTransaction;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentTransactionRepository extends ReactiveCrudRepository<PaymentTransaction, Long> {
    Mono<PaymentTransaction> findFirstByFromAccountOrderByIdDesc(String fromAccount);
}
