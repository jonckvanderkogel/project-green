package project.green.configuration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import project.green.kafka.payments.PaymentEvent;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;

import java.util.stream.IntStream;

import static project.green.entity.Currency.EUR;
import static project.green.simulation.PaymentEventSupport.generatePaymentEvent;

public class PaymentKafkaConsumerConfigTest {

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

    private PaymentEvent generatePaymentEventWithFaultyIban() {
        PaymentEvent faulty = generatePaymentEvent();
        faulty.setToAccount("foo");
        return faulty;
    }
}
