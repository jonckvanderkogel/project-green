package project.green.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import project.green.entity.PaymentTransaction;
import project.green.entity.PaymentTransactionFactory;
import project.green.kafka.payments.PaymentEvent;
import project.green.repository.PaymentTransactionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static project.green.simulation.PaymentEventSupport.generatePaymentEvent;
import static project.green.support.HashingSupport.hashingService;

public class PaymentTransactionPersistenceServiceTest {
    @Test
    public void previousBlockHashShouldBeGenesisIfFirstPaymentEvent() {
        PaymentTransactionRepository paymentTransactionRepository = mock(PaymentTransactionRepository.class);
        PaymentTransactionFactory factory = new PaymentTransactionFactory(hashingService());
        Flux<PaymentEvent> flux = Flux.just(generatePaymentEvent());

        Mockito
            .when(paymentTransactionRepository
                .findFirstByFromAccountOrderByIdDesc(anyString())
            )
            .thenReturn(Mono.empty());

        Mockito
            .when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArguments()[0]));

        PaymentTransactionPersistenceService service = new PaymentTransactionPersistenceService(paymentTransactionRepository, factory, flux);

        StepVerifier
            .create(service.handlePaymentEvents())
            .expectNextMatches(p -> p.getPreviousBlockHash().equals("c6fdd7a7f70862b36a26ccd14752268061e98103299b28fe7763bd9629926f4b"))
            .expectComplete()
            .verify();
    }

    @Test
    public void previousBlockHashShouldMatchWhenNonGenesis() {
        PaymentTransactionRepository paymentTransactionRepository = mock(PaymentTransactionRepository.class);
        PaymentTransactionFactory factory = new PaymentTransactionFactory(hashingService());

        PaymentEvent paymentEvent1 = generatePaymentEvent();

        PaymentEvent paymentEvent2 = generatePaymentEvent();

        PaymentTransaction paymentTransaction1 = factory.createPaymentTransaction(paymentEvent1);

        Flux<PaymentEvent> flux = Flux.just(paymentEvent2);

        Mockito
            .when(paymentTransactionRepository
                .findFirstByFromAccountOrderByIdDesc(anyString())
            )
            .thenReturn(Mono.just(paymentTransaction1));

        Mockito
            .when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArguments()[0]));

        PaymentTransactionPersistenceService service = new PaymentTransactionPersistenceService(paymentTransactionRepository, factory, flux);

        StepVerifier
            .create(service.handlePaymentEvents())
            .expectNextMatches(p -> p.getPreviousBlockHash().equals(paymentTransaction1.getBlockHash()))
            .expectComplete()
            .verify();
    }
}
