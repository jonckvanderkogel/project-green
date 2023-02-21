package project.green.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.green.dto.PaymentTransactionDTO;
import project.green.service.PaymentTransactionFetchingService;
import project.green.simulation.SimulationService;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/simulation")
public class SimulationController {
    private final SimulationService service;

    @GetMapping(value="/start")
    public void start() {
        service.start();
    }

    @GetMapping(value="/stop")
    public void stop() {
        service.stop();
    }

    @GetMapping(value="/create")
    public void generateTransactions(final @RequestParam(name="account") String account,
                                     final @RequestParam(name="transactions") int numberOfTransactions) {
        service.generateAndSendPaymentEventForAccount(account, numberOfTransactions);
    }
}
