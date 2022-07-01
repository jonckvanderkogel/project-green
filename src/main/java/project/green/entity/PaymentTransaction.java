package project.green.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;
import project.green.domain.Block;
import project.green.domain.Currency;
import project.green.kafka.payments.PaymentEvent;

import javax.persistence.Id;
import java.time.ZonedDateTime;
import java.util.function.Function;

@Builder
@Data
@Table("payment_transaction")
public class PaymentTransaction implements Block {
    @Id
    private Long id;
    private String fromAccount;
    private String toAccount;
    private String nameFrom;
    private String nameTo;
    private Double value;
    private Currency currency;
    private ZonedDateTime transactionDateTime;
    private String message;
    private String paymentReference;
    private String extraDescription;
    private String blockHash;
    private String previousBlockHash;

    public static PaymentTransaction of(PaymentEvent paymentEvent, Function<Byte[], String> hashingFun) {
        return PaymentTransaction.builder()
                .fromAccount(paymentEvent.getFromAccount())
                .toAccount(paymentEvent.getToAccount())
                .nameFrom(paymentEvent.getNameFrom())
                .nameTo(paymentEvent.getNameTo())
                .value(paymentEvent.getValue())
                .currency(paymentEvent.getCurrency())
                .transactionDateTime(paymentEvent.getTransactionDateTime())
                .message(paymentEvent.getMessage())
                .paymentReference(paymentEvent.getPaymentReference())
                .extraDescription(paymentEvent.getExtraDescription())
                .build();
    }

    @Override
    public String blockHash() {
        return blockHash;
    }

    @Override
    public String previousBlockHash() {
        return previousBlockHash;
    }

    @Override
    public ZonedDateTime blockDateTime() {
        return transactionDateTime;
    }
}
