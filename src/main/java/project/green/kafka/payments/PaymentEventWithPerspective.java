package project.green.kafka.payments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import project.green.kafka.constraints.ValidIBAN;

@ToString
@Getter
@Setter
public class PaymentEventWithPerspective extends PaymentEvent {
    @ValidIBAN
    private String perspectiveAccount;

    public PaymentEventWithPerspective(PaymentEvent origin, String perspectiveAccount) {
        this.perspectiveAccount = perspectiveAccount;
        this.setFromAccount(origin.getFromAccount());
        this.setToAccount(origin.getToAccount());
        this.setFromName(origin.getFromName());
        this.setToName(origin.getToName());
        this.setValue(origin.getValue());
        this.setCurrency(origin.getCurrency());
        this.setTransactionDateTime(origin.getTransactionDateTime());
        this.setMessage(origin.getMessage());
        this.setPaymentReference(origin.getPaymentReference());
        this.setExtraDescription(origin.getExtraDescription());
    }
}
