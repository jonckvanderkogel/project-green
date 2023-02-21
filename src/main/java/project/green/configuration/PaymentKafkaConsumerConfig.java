package project.green.configuration;

import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import project.green.entity.PaymentTransactionFactory;
import project.green.kafka.payments.PaymentEvent;
import project.green.repository.PaymentTransactionRepository;
import project.green.service.PaymentTransactionPersistenceService;
import project.green.service.ValidationService;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Configuration
public class PaymentKafkaConsumerConfig {
    @Bean
    public ReceiverOptions<String, PaymentEvent> kafkaReceiverOptions(
            @Value(value = "${payments.topic}") String topic,
            @Value(value = "${kafka.commit.interval}") Duration commitInterval,
            @Value(value = "${kafka.commit.batch-size}") Integer batchSize,
            KafkaProperties kafkaProperties) {
        ReceiverOptions<String, PaymentEvent> basicReceiverOptions = ReceiverOptions
                .create(kafkaProperties.buildConsumerProperties());

        return basicReceiverOptions
                .commitInterval(commitInterval)
                .commitBatchSize(batchSize)
                .subscription(Collections.singletonList(topic));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, PaymentEvent> reactiveKafkaConsumerTemplate(ReceiverOptions<String, PaymentEvent> kafkaReceiverOptions) {
        return new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);
    }

    @Bean
    public Flux<Tuple2<PaymentEvent, ReceiverOffset>> paymentEventFlux(@Autowired ReactiveKafkaConsumerTemplate<String, PaymentEvent> kafkaConsumerTemplate) {
        return kafkaConsumerTemplate
            .receive()
            .share()
            .flatMap(receiverRecord -> ValidationService
                    .validate(receiverRecord.value())
                    .map(paymentEvent -> new Tuple2<>(paymentEvent, receiverRecord.receiverOffset())))
            .onErrorContinue((t, o) -> {
                log.error("Error in payment stream.", t);
                log.error("Error happened on object: {}", o);
            });
    }

    @Bean
    public PaymentTransactionPersistenceService paymentTransactionPersistenceService(@Autowired PaymentTransactionRepository paymentTransactionRepository,
                                                                                     @Autowired PaymentTransactionFactory paymentTransactionFactory,
                                                                                     @Autowired Flux<Tuple2<PaymentEvent, ReceiverOffset>> paymentEventFlux) {
        return new PaymentTransactionPersistenceService(paymentTransactionRepository, paymentTransactionFactory, paymentEventFlux);
    }
}
