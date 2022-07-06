package project.green.entity;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import project.green.domain.GenesisBlock;
import project.green.kafka.payments.PaymentEvent;
import project.green.service.HashingService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static project.green.domain.Currency.EUR;
import static project.green.support.HashingSupport.hashingService;
import static project.green.support.PaymentEventSupport.generatePaymentEvent;

@Slf4j
public class PaymentTransactionFactoryTest {
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Test
    public void nextPaymentTransactionHasPreviousBlockHash() {
        PaymentTransactionFactory factory = new PaymentTransactionFactory(hashingService());

        PaymentTransaction firstTransaction = factory.createPaymentTransaction(generatePaymentEvent());
        PaymentTransaction secondTransaction = factory.createPaymentTransaction(generatePaymentEvent(), firstTransaction);
        PaymentTransaction thirdTransaction = factory.createPaymentTransaction(generatePaymentEvent(), secondTransaction);

        assertEquals(GenesisBlock.INSTANCE.blockHash(), firstTransaction.getPreviousBlockHash());
        assertEquals(firstTransaction.blockHash(), secondTransaction.getPreviousBlockHash());
        assertEquals(secondTransaction.blockHash(), thirdTransaction.getPreviousBlockHash());
    }

    @Test
    public void blockHashShouldBeCalculatedAccordingToAlgorithm() {
        HashingService hashingService = hashingService();
        PaymentTransactionFactory factory = new PaymentTransactionFactory(hashingService);

        PaymentEvent paymentEvent = new PaymentEvent(
            "NL39INGB1897063432",
            "NL29INGB7591500313",
            "Trisha Funk",
            "Therese Greenholt PhD",
            100d,
            EUR,
            ZonedDateTime.of(LocalDateTime.of(2022, 7, 6, 17, 2), ZoneId.of("GMT+01:00")),
            "message",
            "12345",
            "extraDescription"
        );

        String concatenatedHashes = hashingService.hash("NL39INGB1897063432".getBytes(StandardCharsets.UTF_8))
            .concat(hashingService.hash("NL29INGB7591500313".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("Trisha Funk".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("Therese Greenholt PhD".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash(new Byte[]{Double.valueOf(100d).byteValue()}))
            .concat(hashingService.hash(EUR.toString().getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash(ZonedDateTime.of(LocalDateTime.of(2022, 7, 6, 17, 2), ZoneId.of("GMT+01:00")).format(FORMATTER).getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("message".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("12345".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("extraDescription".getBytes(StandardCharsets.UTF_8)))
            .concat(GenesisBlock.INSTANCE.blockHash());

        String blockHash = hashingService.hash(concatenatedHashes.getBytes(StandardCharsets.UTF_8));

        assertEquals(blockHash, factory.createPaymentTransaction(paymentEvent).getBlockHash());
    }

    @Test
    public void blockHashShouldDependOnPreviousBlockHash() {
        HashingService hashingService = hashingService();
        PaymentTransactionFactory factory = new PaymentTransactionFactory(hashingService);

        PaymentEvent paymentEvent1 = generatePaymentEvent();

        PaymentEvent paymentEvent2 = new PaymentEvent(
            "NL39INGB1897063432",
            "NL77INGB7224894642",
            "Trisha Funk",
            "Gandalf",
            150d,
            EUR,
            ZonedDateTime.of(LocalDateTime.of(2022, 7, 6, 18, 4), ZoneId.of("GMT+01:00")),
            "messageTwo",
            "123456",
            "extraDescription2"
        );

        PaymentTransaction paymentTransaction1 = factory.createPaymentTransaction(paymentEvent1);
        PaymentTransaction paymentTransaction2 = factory.createPaymentTransaction(paymentEvent2, paymentTransaction1);

        String concatenatedHashes = hashingService.hash("NL39INGB1897063432".getBytes(StandardCharsets.UTF_8))
            .concat(hashingService.hash("NL77INGB7224894642".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("Trisha Funk".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("Gandalf".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash(new Byte[]{Double.valueOf(150d).byteValue()}))
            .concat(hashingService.hash(EUR.toString().getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash(ZonedDateTime.of(LocalDateTime.of(2022, 7, 6, 18, 4), ZoneId.of("GMT+01:00")).format(FORMATTER).getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("messageTwo".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("123456".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("extraDescription2".getBytes(StandardCharsets.UTF_8)))
            .concat(paymentTransaction1.blockHash());

        String blockHash = hashingService.hash(concatenatedHashes.getBytes(StandardCharsets.UTF_8));

        assertEquals(blockHash, paymentTransaction2.getBlockHash());
    }
}
