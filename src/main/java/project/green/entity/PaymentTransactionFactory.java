package project.green.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.green.kafka.payments.PaymentEventWithPerspective;
import project.green.service.HashingService;

@Component
@RequiredArgsConstructor
public class PaymentTransactionFactory {
    private final HashingService hashingService;

    private final static PaymentTransaction GENESIS = PaymentTransaction
        .builder()
        .blockHash(GenesisBlock.INSTANCE.blockHash())
        .build();

    public PaymentTransaction createPaymentTransaction(PaymentEventWithPerspective paymentEvent) {
        return createPaymentTransaction(paymentEvent, GENESIS);
    }

    public PaymentTransaction createPaymentTransaction(PaymentEventWithPerspective paymentEvent, PaymentTransaction previousPaymentTransaction) {
        return PaymentTransaction.builder()
            .perspectiveAccount(paymentEvent.getPerspectiveAccount())
            .fromAccount(paymentEvent.getFromAccount())
            .toAccount(paymentEvent.getToAccount())
            .fromName(paymentEvent.getFromName())
            .toName(paymentEvent.getToName())
            .value(paymentEvent.getValue())
            .currency(paymentEvent.getCurrency())
            .transactionDateTime(paymentEvent.getTransactionDateTime())
            .message(paymentEvent.getMessage())
            .paymentReference(paymentEvent.getPaymentReference())
            .extraDescription(paymentEvent.getExtraDescription())
            .blockHash(hashingService.calculateBlockHash(paymentEvent, previousPaymentTransaction))
            .previousBlockHash(previousPaymentTransaction.getBlockHash())
            .build();
    }
}
