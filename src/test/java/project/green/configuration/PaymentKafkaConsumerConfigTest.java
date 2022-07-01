package project.green.configuration;

import com.github.javafaker.Faker;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import project.green.kafka.payments.PaymentEvent;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.IntStream;

import static project.green.domain.Currency.EUR;

public class PaymentKafkaConsumerConfigTest {
    private static final Faker FAKER = new Faker();

    @Test
    public void paymentEventFluxWithValidElementsShouldBeFine() {
        ReactiveKafkaConsumerTemplate<String, PaymentEvent> mockedConsumerTemplate = Mockito.mock(ReactiveKafkaConsumerTemplate.class);
        Flux<ReceiverRecord<String, PaymentEvent>> receiverRecordFlux = Flux.fromStream(IntStream.range(0, 10).mapToObj(i -> generateReceiverRecord(generateConsumerRecord(generatePaymentEvent()))));

        Mockito.when(mockedConsumerTemplate.receive()).thenReturn(receiverRecordFlux);

        StepVerifier.create(new PaymentKafkaConsumerConfig().paymentEventFlux(mockedConsumerTemplate))
                .expectNextMatches(pe -> pe.getCurrency().equals(EUR))
                .expectNextCount(9)
                .expectComplete()
                .verify();
    }

    @Test
    public void paymentEventFluxShouldDropInvalidElements() {
        ReactiveKafkaConsumerTemplate<String, PaymentEvent> mockedConsumerTemplate = Mockito.mock(ReactiveKafkaConsumerTemplate.class);
        Flux<ReceiverRecord<String, PaymentEvent>> receiverRecordFlux = Flux.just(
                generateReceiverRecord(generateConsumerRecord(generatePaymentEvent())),
                generateReceiverRecord(generateConsumerRecord(generatePaymentEventWithFaultyIban())),
                generateReceiverRecord(generateConsumerRecord(generatePaymentEvent()))
        );

        Mockito.when(mockedConsumerTemplate.receive()).thenReturn(receiverRecordFlux);

        StepVerifier.create(new PaymentKafkaConsumerConfig().paymentEventFlux(mockedConsumerTemplate))
                .expectNextCount(2)
                .expectComplete()
                .verify();
    }

    private ReceiverRecord<String, PaymentEvent> generateReceiverRecord(ConsumerRecord<String, PaymentEvent> consumerRecord) {
        return new ReceiverRecord<>(consumerRecord, null);
    }

    private ConsumerRecord<String, PaymentEvent> generateConsumerRecord(PaymentEvent paymentEvent) {
        return new ConsumerRecord<>("topic", 0, 123L, "key", paymentEvent);
    }

    private PaymentEvent generatePaymentEvent() {
        return new PaymentEvent(
                new Iban.Builder()
                        .countryCode(CountryCode.NL)
                        .bankCode("INGB")
                        .buildRandom()
                        .toString(),
                new Iban.Builder()
                        .countryCode(CountryCode.NL)
                        .bankCode("INGB")
                        .buildRandom()
                        .toString(),
                FAKER.name().fullName(),
                FAKER.name().fullName(),
                100d,
                EUR,
                ZonedDateTime.of(LocalDateTime.of(0, 1, 1, 0, 0), ZoneId.of("GMT+01:00")),
                FAKER.dune().quote(),
                "12345",
                FAKER.hobbit().quote()
        );
    }

    private PaymentEvent generatePaymentEventWithFaultyIban() {
        PaymentEvent faulty = generatePaymentEvent();
        faulty.setToAccount("foo");
        return faulty;
    }
}
