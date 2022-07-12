package project.green.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import project.green.entity.PaymentTransaction;

@Getter
@RequiredArgsConstructor
public class PaymentTransactionDTO {
    private final PaymentTransaction paymentTransaction;
    private final String signature;
}
