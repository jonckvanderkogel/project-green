package project.green.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.green.service.MessageProducerService;

@Service
@RequiredArgsConstructor
public class SimulationService {
    private final MessageProducerService messageProducerService;


}
