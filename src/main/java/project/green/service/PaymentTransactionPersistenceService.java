package project.green.service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import project.green.entity.PaymentTransaction;
import project.green.entity.PaymentTransactionFactory;
import project.green.kafka.payments.PaymentEvent;
import project.green.kafka.payments.PaymentEventWithPerspective;
import project.green.repository.PaymentTransactionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
public class PaymentTransactionPersistenceService {
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentTransactionFactory paymentTransactionFactory;
    private final Flux<PaymentEvent> paymentEventFlux;

    @PostConstruct
    private void postConstruct() {
        handlePaymentEvents().subscribe();
    }

    public Flux<PaymentTransaction> handlePaymentEvents() {
        return paymentEventFlux
            .flatMap(this::duplicateWithPerspective)
            .flatMap(this::persistPaymentEvent);
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        timeout = 5
    )
    public Mono<PaymentTransaction> persistPaymentEvent(PaymentEventWithPerspective paymentEvent) {
        return paymentTransactionRepository
            .findFirstByPerspectiveAccountOrderByIdDesc(paymentEvent.getPerspectiveAccount())
            .map(previousTransaction -> paymentTransactionFactory.createPaymentTransaction(paymentEvent, previousTransaction))
            .defaultIfEmpty(paymentTransactionFactory.createPaymentTransaction(paymentEvent))
            .flatMap(paymentTransactionRepository::save);
    }



    private Flux<PaymentEventWithPerspective> duplicateWithPerspective(PaymentEvent origin) {
        return Flux.just(
            new PaymentEventWithPerspective(origin, origin.getFromAccount()),
            new PaymentEventWithPerspective(origin, origin.getToAccount())
        );
    }

}
