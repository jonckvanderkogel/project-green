package project.green.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import project.green.dto.PaymentTransactionDTO;
import project.green.dto.PaymentTransactionWrapper;
import project.green.repository.OffsetRepository;
import project.green.repository.PaymentTransactionRepository;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class PaymentTransactionFetchingService {
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OffsetRepository offsetRepository;
    private final SigningService signingService;

    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        timeout = 5
    )
    public Flux<PaymentTransactionWrapper> fetchPaymentTransactions(String account) {
        Flux<PaymentTransactionWrapper> paymentTransactionFlux = paymentTransactionRepository
            .findByAccount(account)
            .flatMap(pt -> signingService
                .sign(pt.getBlockHash().getBytes(StandardCharsets.UTF_8))
                .map(signature -> new PaymentTransactionWrapper(new PaymentTransactionDTO(pt), signature))
            )
            .share();

        paymentTransactionFlux
            .reduce((p1, p2) -> p2)
            .flatMap(lastTransaction -> offsetRepository.save(
                account, lastTransaction.getPaymentTransaction().getId()
            ))
            .subscribe();

        return paymentTransactionFlux;
    }
}
