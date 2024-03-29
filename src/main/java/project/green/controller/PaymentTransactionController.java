package project.green.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.green.dto.PaymentTransactionWrapper;
import project.green.service.PaymentTransactionFetchingService;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transactions")
public class PaymentTransactionController {
    private final PaymentTransactionFetchingService service;

    @GetMapping
    public Flux<PaymentTransactionWrapper> getTransactions(final @RequestParam(name = "account") String account) {
        return service.fetchPaymentTransactions(account);
    }

}
