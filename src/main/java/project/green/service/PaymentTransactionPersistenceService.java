package project.green.service;

import io.vavr.Tuple2;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import project.green.entity.PaymentTransaction;
import project.green.entity.PaymentTransactionFactory;
import project.green.kafka.payments.PaymentEvent;
import project.green.kafka.payments.PaymentEventWithPerspective;
import project.green.repository.PaymentTransactionRepository;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOffset;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@RequiredArgsConstructor
public class PaymentTransactionPersistenceService {
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentTransactionFactory paymentTransactionFactory;
    private final Flux<Tuple2<PaymentEvent, ReceiverOffset>> paymentEventFlux;

    private Disposable paymentEventFluxDisposable;

    @PostConstruct
    private void postConstruct() {
        paymentEventFluxDisposable = handlePaymentEvents().subscribe();
    }

    @PreDestroy
    private void preDestroy() {
        paymentEventFluxDisposable.dispose();
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
    public Mono<PaymentTransaction> persistPaymentEvent(Tuple2<PaymentEventWithPerspective, ReceiverOffset> tuple) {
        return paymentTransactionRepository
                .findFirstByPerspectiveAccountOrderByIdDesc(tuple._1().getPerspectiveAccount())
                .map(previousTransaction -> paymentTransactionFactory.createPaymentTransaction(tuple._1(), previousTransaction))
                .switchIfEmpty(Mono.defer(() -> Mono.just(paymentTransactionFactory.createPaymentTransaction(tuple._1()))))
                .flatMap(paymentTransactionRepository::save)
                .doOnNext(pt -> tuple._2().acknowledge());
    }


    private Flux<Tuple2<PaymentEventWithPerspective, ReceiverOffset>> duplicateWithPerspective(Tuple2<PaymentEvent, ReceiverOffset> tuple) {
        return Flux.just(
                new Tuple2<>(new PaymentEventWithPerspective(tuple._1(), tuple._1().getFromAccount()), tuple._2()),
                new Tuple2<>(new PaymentEventWithPerspective(tuple._1(), tuple._1().getToAccount()), tuple._2())
        );
    }

}
