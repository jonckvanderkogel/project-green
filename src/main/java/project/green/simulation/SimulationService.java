package project.green.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.green.service.MessageProducerService;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class SimulationService {
    private final MessageProducerService messageProducerService;
    private final SimulationProvider simulationProvider;

    @PostConstruct
    public void runSimulation() {
        simulationProvider.generatePaymentEvents()
            .flatMap(messageProducerService::sendMessage)
            .subscribe();
    }
}
