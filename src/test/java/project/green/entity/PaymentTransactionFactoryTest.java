package project.green.entity;

import org.junit.jupiter.api.Test;
import project.green.kafka.payments.PaymentEvent;
import project.green.kafka.payments.PaymentEventWithPerspective;
import project.green.service.HashingService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static project.green.entity.Currency.EUR;
import static project.green.simulation.PaymentEventSupport.generatePaymentEvent;
import static project.green.simulation.PaymentEventSupport.generatePaymentEventWithPerspective;
import static project.green.support.HashingSupport.hashingService;
import static project.green.util.DateTimeUtil.FORMATTER;

public class PaymentTransactionFactoryTest {

    @Test
    public void nextPaymentTransactionHasPreviousBlockHash() {
        PaymentTransactionFactory factory = new PaymentTransactionFactory(hashingService());

        PaymentTransaction firstTransaction = factory.createPaymentTransaction(generatePaymentEventWithPerspective());
        PaymentTransaction secondTransaction = factory.createPaymentTransaction(generatePaymentEventWithPerspective(), firstTransaction);
        PaymentTransaction thirdTransaction = factory.createPaymentTransaction(generatePaymentEventWithPerspective(), secondTransaction);

        assertEquals(GenesisBlock.INSTANCE.blockHash(), firstTransaction.getPreviousBlockHash());
        assertEquals(firstTransaction.blockHash(), secondTransaction.getPreviousBlockHash());
        assertEquals(secondTransaction.blockHash(), thirdTransaction.getPreviousBlockHash());
    }

    @Test
    public void blockHashShouldBeCalculatedAccordingToAlgorithm() {
        HashingService hashingService = hashingService();
        PaymentTransactionFactory factory = new PaymentTransactionFactory(hashingService);


        PaymentEventWithPerspective paymentEvent = new PaymentEventWithPerspective(
            new PaymentEvent(
                "NL39INGB1897063432",
                "NL29INGB7591500313",
                "Trisha Funk",
                "Therese Greenholt PhD",
                10000L,
                EUR,
                ZonedDateTime.of(LocalDateTime.of(2022, 7, 6, 17, 2), ZoneId.of("Europe/Paris")),
                "message",
                "12345",
                "extraDescription"
            ),
            "NL39INGB1897063432"
        );

        String concatenatedHashes = hashingService.hash("NL39INGB1897063432".getBytes(StandardCharsets.UTF_8))
            .concat(hashingService.hash("NL39INGB1897063432".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("NL29INGB7591500313".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("Trisha Funk".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("Therese Greenholt PhD".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash(String.valueOf(10000L).getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash(EUR.toString().getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash(ZonedDateTime.of(LocalDateTime.of(2022, 7, 6, 17, 2), ZoneId.of("Europe/Paris")).format(FORMATTER).getBytes(StandardCharsets.UTF_8)))
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
        PaymentEventWithPerspective paymentEventWithPerspective1 = new PaymentEventWithPerspective(
            paymentEvent1,
            paymentEvent1.getFromAccount()
        );

        PaymentEventWithPerspective paymentEventWithPerspective2 = new PaymentEventWithPerspective(
            new PaymentEvent(
                "NL39INGB1897063432",
                "NL77INGB7224894642",
                "Trisha Funk",
                "Gandalf",
                15000L,
                EUR,
                ZonedDateTime.of(LocalDateTime.of(2022, 7, 6, 18, 4), ZoneId.of("Europe/Paris")),
                "messageTwo",
                "123456",
                "extraDescription2"
            ),
            "NL39INGB1897063432"
        );

        PaymentTransaction paymentTransaction1 = factory.createPaymentTransaction(paymentEventWithPerspective1);
        PaymentTransaction paymentTransaction2 = factory.createPaymentTransaction(paymentEventWithPerspective2, paymentTransaction1);

        String concatenatedHashes = hashingService.hash("NL39INGB1897063432".getBytes(StandardCharsets.UTF_8))
            .concat(hashingService.hash("NL39INGB1897063432".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("NL77INGB7224894642".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("Trisha Funk".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("Gandalf".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash(String.valueOf(15000L).getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash(EUR.toString().getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash(ZonedDateTime.of(LocalDateTime.of(2022, 7, 6, 18, 4), ZoneId.of("Europe/Paris")).format(FORMATTER).getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("messageTwo".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("123456".getBytes(StandardCharsets.UTF_8)))
            .concat(hashingService.hash("extraDescription2".getBytes(StandardCharsets.UTF_8)))
            .concat(paymentTransaction1.blockHash());

        String blockHash = hashingService.hash(concatenatedHashes.getBytes(StandardCharsets.UTF_8));

        assertEquals(blockHash, paymentTransaction2.getBlockHash());
    }
}
