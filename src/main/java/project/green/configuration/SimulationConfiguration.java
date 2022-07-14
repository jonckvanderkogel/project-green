package project.green.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import project.green.repository.PaymentTransactionRepository;
import project.green.service.MessageProducerService;
import project.green.simulation.SimulationService;

@Configuration
public class SimulationConfiguration {

    @Bean
    @DependsOn("liquibase")
    public SimulationService simulationService(
        MessageProducerService messageProducerService,
        PaymentTransactionRepository paymentTransactionRepository) {
        return new SimulationService(messageProducerService, paymentTransactionRepository);
    }
}
