package project.green.service;

import io.vavr.Tuple2;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import project.green.entity.PaymentTransaction;
import project.green.entity.PaymentTransactionFactory;
import project.green.kafka.payments.PaymentEvent;
import project.green.kafka.payments.PaymentEventWithPerspective;
import project.green.repository.PaymentTransactionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static project.green.simulation.PaymentEventSupport.*;
import static project.green.support.HashingSupport.hashingService;

public class PaymentTransactionPersistenceServiceTest {
    @Test
    public void previousBlockHashShouldBeGenesisIfFirstPaymentEvent() {
        PaymentTransactionRepository paymentTransactionRepository = mock(PaymentTransactionRepository.class);
        PaymentTransactionFactory factory = new PaymentTransactionFactory(hashingService());
        Flux<Tuple2<PaymentEvent, ReceiverOffset>> flux = Flux.just(generateTuple());

        Mockito
            .when(paymentTransactionRepository
                .findFirstByPerspectiveAccountOrderByIdDesc(anyString())
            )
            .thenReturn(Mono.empty());

        Mockito
            .when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArguments()[0]));

        PaymentTransactionPersistenceService service = new PaymentTransactionPersistenceService(paymentTransactionRepository, factory, flux);

        // Both the generated paymentEventsWithPerspective should have genesis as it's previous block hash since
        // they both from their own chain from the customers' perspective.
        StepVerifier
            .create(service.handlePaymentEvents())
            .expectNextMatches(p -> p.getPreviousBlockHash().equals("c6fdd7a7f70862b36a26ccd14752268061e98103299b28fe7763bd9629926f4b"))
            .expectNextMatches(p -> p.getPreviousBlockHash().equals("c6fdd7a7f70862b36a26ccd14752268061e98103299b28fe7763bd9629926f4b"))
            .expectComplete()
            .verify();
    }

    @Test
    public void previousBlockHashShouldMatchWhenNonGenesis() {
        PaymentTransactionRepository paymentTransactionRepository = mock(PaymentTransactionRepository.class);
        PaymentTransactionFactory factory = new PaymentTransactionFactory(hashingService());

        PaymentEventWithPerspective paymentEvent11 = generatePaymentEventWithPerspective("NL24INGB8196349335");
        PaymentEventWithPerspective paymentEvent12 = generatePaymentEventWithPerspective(paymentEvent11, paymentEvent11.getToAccount());

        PaymentEvent paymentEvent2 = generatePaymentEventFromToAccount(paymentEvent11.getFromAccount(), paymentEvent11.getToAccount());

        PaymentTransaction paymentTransaction11 = factory.createPaymentTransaction(paymentEvent11);
        PaymentTransaction paymentTransaction12 = factory.createPaymentTransaction(paymentEvent12);

        Flux<Tuple2<PaymentEvent, ReceiverOffset>> flux = Flux.just(new Tuple2<>(paymentEvent2, generateReceiverOffset()));

        Mockito
            .when(paymentTransactionRepository
                .findFirstByPerspectiveAccountOrderByIdDesc(paymentTransaction11.getPerspectiveAccount())
            )
            .thenReturn(Mono.just(paymentTransaction11));

        Mockito
            .when(paymentTransactionRepository
                .findFirstByPerspectiveAccountOrderByIdDesc(paymentTransaction12.getPerspectiveAccount())
            )
            .thenReturn(Mono.just(paymentTransaction12));

        Mockito
            .when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArguments()[0]));

        PaymentTransactionPersistenceService service = new PaymentTransactionPersistenceService(paymentTransactionRepository, factory, flux);

        StepVerifier
            .create(service.handlePaymentEvents())
            .expectNextMatches(p -> p.getPreviousBlockHash().equals(paymentTransaction11.getBlockHash()))
            .expectNextMatches(p -> p.getPreviousBlockHash().equals(paymentTransaction12.getBlockHash()))
            .expectComplete()
            .verify();
    }
}
