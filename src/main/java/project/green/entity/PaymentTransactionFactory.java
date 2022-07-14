package project.green.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.green.kafka.payments.PaymentEventWithPerspective;
import project.green.service.HashingService;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentTransactionFactory {
    private final HashingService hashingService;
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private final static List<Function<PaymentEventWithPerspective, Byte[]>> PAYMENT_BYTE_FUN = List.of(
        (pe) -> fromPrimByteArray(pe.getPerspectiveAccount().getBytes(StandardCharsets.UTF_8)),
        (pe) -> fromPrimByteArray(pe.getFromAccount().getBytes(StandardCharsets.UTF_8)),
        (pe) -> fromPrimByteArray(pe.getToAccount().getBytes(StandardCharsets.UTF_8)),
        (pe) -> fromPrimByteArray(pe.getFromName().getBytes(StandardCharsets.UTF_8)),
        (pe) -> fromPrimByteArray(pe.getToName().getBytes(StandardCharsets.UTF_8)),
        (pe) -> new Byte[]{pe.getValue().byteValue()},
        (pe) -> fromPrimByteArray(pe.getCurrency().toString().getBytes(StandardCharsets.UTF_8)),
        (pe) -> fromPrimByteArray(pe.getTransactionDateTime().format(FORMATTER).getBytes(StandardCharsets.UTF_8)),
        (pe) -> fromPrimByteArray(pe.getMessage().getBytes(StandardCharsets.UTF_8)),
        (pe) -> fromPrimByteArray(pe.getPaymentReference().getBytes(StandardCharsets.UTF_8)),
        (pe) -> fromPrimByteArray(pe.getExtraDescription().getBytes(StandardCharsets.UTF_8))
    );
    private final static PaymentTransaction GENESIS = PaymentTransaction
        .builder()
        .blockHash(GenesisBlock.INSTANCE.blockHash())
        .build();

    private static Byte[] fromPrimByteArray(byte[] bytes) {
        Byte[] byteObjects = new Byte[bytes.length];

        int i=0;
        for (byte b : bytes)
            byteObjects[i++] = b;

        return byteObjects;
    }

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
            .blockHash(calculateBlockHash(paymentEvent, previousPaymentTransaction))
            .previousBlockHash(previousPaymentTransaction.getBlockHash())
            .build();
    }

    private String calculateBlockHash(PaymentEventWithPerspective paymentEvent, PaymentTransaction previousPaymentTransaction) {
        String concatenatedHashes = PAYMENT_BYTE_FUN.stream()
            .map(fun -> hashingService.hash(fun.apply(paymentEvent)))
            .collect(Collectors.joining())
            .concat(previousPaymentTransaction.getBlockHash());

        return hashingService.hash(concatenatedHashes.getBytes(StandardCharsets.UTF_8));
    }
}
