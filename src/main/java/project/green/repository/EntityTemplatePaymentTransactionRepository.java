package project.green.repository;

import project.green.entity.PaymentTransaction;
import reactor.core.publisher.Flux;

public interface EntityTemplatePaymentTransactionRepository {
    Flux<PaymentTransaction> findByAccount(String account);
}
