package project.green.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentTransactionWrapper {
    private final PaymentTransactionDTO paymentTransaction;
    private final String signature;
}
