package project.green.dto;

import lombok.Getter;
import project.green.entity.Currency;
import project.green.entity.PaymentTransaction;

import static project.green.util.DateTimeUtil.FORMATTER;

@Getter
public class PaymentTransactionDTO {
    private Long id;
    private String perspectiveAccount;
    private String fromAccount;
    private String toAccount;
    private String fromName;
    private String toName;
    private Long value;
    private Currency currency;
    private String transactionDateTime;
    private String message;
    private String paymentReference;
    private String extraDescription;
    private String blockHash;
    private String previousBlockHash;

    public PaymentTransactionDTO(PaymentTransaction paymentTransaction) {
        this.id = paymentTransaction.getId();
        this.perspectiveAccount = paymentTransaction.getPerspectiveAccount();
        this.fromAccount = paymentTransaction.getFromAccount();
        this.toAccount = paymentTransaction.getToAccount();
        this.fromName = paymentTransaction.getFromName();
        this.toName = paymentTransaction.getToName();
        this.value = paymentTransaction.getValue();
        this.currency = paymentTransaction.getCurrency();
        this.transactionDateTime = paymentTransaction.getTransactionDateTime().format(FORMATTER);
        this.message = paymentTransaction.getMessage();
        this.paymentReference = paymentTransaction.getPaymentReference();
        this.extraDescription = paymentTransaction.getExtraDescription();
        this.blockHash = paymentTransaction.getBlockHash();
        this.previousBlockHash = paymentTransaction.getPreviousBlockHash();
    }
}
