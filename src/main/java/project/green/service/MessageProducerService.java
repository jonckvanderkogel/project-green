package project.green.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import project.green.kafka.payments.PaymentEvent;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

@Slf4j
@Service
public class MessageProducerService {
    private final ReactiveKafkaProducerTemplate<String, PaymentEvent> kafkaTemplate;
    private final String topic;

    public MessageProducerService(@Autowired ReactiveKafkaProducerTemplate<String, PaymentEvent> kafkaTemplate,
                                  @Value("${payments.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public Mono<SenderResult<Void>> sendMessage(PaymentEvent message) {
        log.info(String.format("Producing message: %s", message));
        return kafkaTemplate.send(topic, message);
    }
}
