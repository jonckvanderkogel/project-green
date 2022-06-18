package project.green.kafka.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import project.green.kafka.payments.PaymentEvent;
import project.green.service.ValidationService;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;

@Slf4j
@Configuration
public class PaymentConsumerConfig {
    @Bean
    public ReceiverOptions<String, PaymentEvent> kafkaReceiverOptions(@Value(value = "${payments.topic}") String topic, KafkaProperties kafkaProperties) {
        ReceiverOptions<String, PaymentEvent> basicReceiverOptions = ReceiverOptions.create(kafkaProperties.buildConsumerProperties());
        return basicReceiverOptions.subscription(Collections.singletonList(topic));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, PaymentEvent> reactiveKafkaConsumerTemplate(ReceiverOptions<String, PaymentEvent> kafkaReceiverOptions) {
        return new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);
    }

    @Bean
    public Flux<PaymentEvent> confirmationFlux(@Autowired ReactiveKafkaConsumerTemplate<String, PaymentEvent> kafkaConsumerTemplate) {
        return kafkaConsumerTemplate
            .receive()
            .share()
            .map(ConsumerRecord::value)
            .flatMap(ValidationService::validate)
            .onErrorContinue((t, o) -> {
                log.error("Error in payment stream.", t);
                log.error("Error happened on object: {}", o);
            });
    }
}
