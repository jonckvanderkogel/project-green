package project.green.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import project.green.entity.PaymentTransaction;
import project.green.repository.OffsetRepository;
import project.green.repository.PaymentTransactionRepository;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class PaymentTransactionFetchingService {
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OffsetRepository offsetRepository;

    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        timeout = 5
    )
    public Flux<PaymentTransaction> fetchPaymentTransactions(String account) {
        Flux<PaymentTransaction> paymentTransactionFlux = paymentTransactionRepository
            .findByAccount(account)
            .share();

        paymentTransactionFlux
            .reduce((p1, p2) -> p2)
            .subscribe(lastTransaction -> {
                offsetRepository.save(
                    account, lastTransaction.getId()
                ).subscribe();
            });

        return paymentTransactionFlux;
    }
}
