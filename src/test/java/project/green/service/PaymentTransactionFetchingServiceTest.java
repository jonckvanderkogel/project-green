package project.green.service;

import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import project.green.entity.PaymentTransaction;
import project.green.entity.PaymentTransactionFactory;
import project.green.repository.OffsetRepository;
import project.green.repository.PaymentTransactionRepository;
import project.green.support.HashingSupport;
import project.green.support.SecuritySupport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static project.green.simulation.PaymentEventSupport.generatePaymentEvent;

public class PaymentTransactionFetchingServiceTest {
    private final PaymentTransactionFactory paymentTransactionFactory = new PaymentTransactionFactory(HashingSupport.hashingService());

    @Test
    public void lastPaymentTransactionIdStreamedShouldBeStoredAsOffset() {
        PaymentTransactionRepository paymentTransactionRepository = mock(PaymentTransactionRepository.class);
        OffsetRepository offsetRepository = mock(OffsetRepository.class);
        SigningService signingService = new SigningService(SecuritySupport.getSigningSignatureSupplier(), SecuritySupport.getVerificationSignatureSupplier());

        PaymentTransactionFetchingService service = new PaymentTransactionFetchingService(paymentTransactionRepository, offsetRepository, signingService);

        Mockito
            .when(paymentTransactionRepository.findByAccount("NL24INGB8196349335"))
            .thenReturn(generatePaymentTransactionFlux("NL24INGB8196349335"));

        Mockito
            .when(offsetRepository.save(anyString(), anyLong()))
            .thenReturn(Mono.empty());

        StepVerifier
            .create(service.fetchPaymentTransactions("NL24INGB8196349335"))
            .expectNextCount(10L)
            .expectComplete()
            .verify();

        Mockito.verify(offsetRepository).save("NL24INGB8196349335", 10L);
    }

    @Test
    public void signatureShouldMatchBlockHash() {
        PaymentTransactionRepository paymentTransactionRepository = mock(PaymentTransactionRepository.class);
        OffsetRepository offsetRepository = mock(OffsetRepository.class);
        SigningService signingService = new SigningService(SecuritySupport.getSigningSignatureSupplier(), SecuritySupport.getVerificationSignatureSupplier());

        PaymentTransactionFetchingService service = new PaymentTransactionFetchingService(paymentTransactionRepository, offsetRepository, signingService);

        Mockito
            .when(paymentTransactionRepository.findByAccount("NL24INGB8196349335"))
            .thenReturn(generatePaymentTransactionFlux("NL24INGB8196349335"));

        Mockito
            .when(offsetRepository.save(anyString(), anyLong()))
            .thenReturn(Mono.empty());

        StepVerifier
            .create(service.fetchPaymentTransactions("NL24INGB8196349335")
                .flatMap(p -> signingService.verify(
                    p.getPaymentTransaction().getBlockHash().getBytes(StandardCharsets.UTF_8),
                    p.getSignature())))
            .expectNextMatches(b -> b)
            .expectNextCount(9L)
            .expectComplete()
            .verify();
    }

    private Flux<PaymentTransaction> generatePaymentTransactionFlux(String account) {
        PaymentTransaction genesis = paymentTransactionFactory.createPaymentTransaction(generatePaymentEvent(account));
        genesis.setId(1L);
        return Flux.fromIterable(generatePaymentTransactions(List.of(genesis)));
    }

    private List<PaymentTransaction> generatePaymentTransactions(List<PaymentTransaction> accumulator) {
        if (accumulator.size() == 10) {
            return accumulator;
        } else {
            PaymentTransaction last = accumulator.last();
            PaymentTransaction newTransaction = paymentTransactionFactory.createPaymentTransaction(generatePaymentEvent(last.getFromAccount()), last);
            newTransaction.setId(last.getId() + 1);
            return generatePaymentTransactions(accumulator.append(newTransaction));
        }
    }
}
