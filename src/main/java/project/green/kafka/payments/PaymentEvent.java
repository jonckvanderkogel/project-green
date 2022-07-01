package project.green.kafka.payments;

import lombok.*;
import project.green.domain.Currency;
import project.green.kafka.constraints.ValidIBAN;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    @ValidIBAN
    private String fromAccount;
    @ValidIBAN
    private String toAccount;

    @NotEmpty
    private String nameFrom;

    @NotEmpty
    private String nameTo;

    @NotNull
    private Double value;

    @NotNull
    private Currency currency;

    @NotNull
    private ZonedDateTime transactionDateTime;

    private String message;

    private String paymentReference;

    private String extraDescription;

}
