package project.green.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@ToString
@Builder
@Data
@Table("payment_transaction")
public class PaymentTransaction implements Block {
    @Id
    private Long id;
    private String perspectiveAccount;
    private String fromAccount;
    private String toAccount;
    private String fromName;
    private String toName;
    private Double value;
    private Currency currency;
    private ZonedDateTime transactionDateTime;
    private String message;
    private String paymentReference;
    private String extraDescription;
    private String blockHash;
    private String previousBlockHash;

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
