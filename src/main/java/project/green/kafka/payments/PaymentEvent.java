package project.green.kafka.payments;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import project.green.entity.Currency;
import project.green.kafka.constraints.ValidIBAN;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Builder
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

    @Length(max = 255)
    @NotEmpty
    private String fromName;

    @Length(max = 255)
    @NotEmpty
    private String toName;

    @Min(0)
    @NotNull
    private Long value;

    @NotNull
    private Currency currency;

    @NotNull
    private ZonedDateTime transactionDateTime;

    @Length(max = 255)
    private String message;

    @Length(max = 255)
    private String paymentReference;

    @Length(max = 255)
    private String extraDescription;
}
