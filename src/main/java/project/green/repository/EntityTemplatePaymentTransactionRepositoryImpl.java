package project.green.repository;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import project.green.entity.Currency;
import project.green.entity.PaymentTransaction;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.Map;

public record EntityTemplatePaymentTransactionRepositoryImpl(R2dbcEntityTemplate template) implements EntityTemplatePaymentTransactionRepository {

    public Flux<PaymentTransaction> findByAccount(String account) {
        return template.getDatabaseClient()
            .sql(
                """
                        SELECT
                            id,
                            from_account,
                            to_account,
                            name_from,
                            name_to,
                            value,
                            currency,
                            transaction_date_time,
                            message,
                            payment_reference,
                            extra_description,
                            block_hash,
                            previous_block_hash
                        FROM
                            payment_transaction
                        WHERE
                            (
                                from_account=$1
                            OR  to_account=$1)
                        AND id >
                            (   SELECT
                                    COALESCE(MAX(transaction_id), 0) AS tr_id
                                FROM
                                    offset_table
                                WHERE
                                    account=$1)
                    """
            )
            .bind("$1", account)
            .fetch()
            .all()
            .map(this::constructPaymentTransaction);
    }

    private PaymentTransaction constructPaymentTransaction(Map<String, Object> data) {
        return PaymentTransaction.builder()
            .id((Long) data.get("id"))
            .fromAccount((String) data.get("from_account"))
            .toAccount((String) data.get("to_account"))
            .nameFrom((String) data.get("name_from"))
            .nameTo((String) data.get("name_to"))
            .value((Double) data.get("value"))
            .currency(Currency.valueOf((String) data.get("currency")))
            .transactionDateTime(((OffsetDateTime) data.get("transaction_date_time")).toZonedDateTime())
            .message((String) data.get("message"))
            .paymentReference((String) data.get("payment_reference"))
            .extraDescription((String) data.get("extra_description"))
            .blockHash((String) data.get("block_hash"))
            .previousBlockHash((String) data.get("previous_block_hash"))
            .build();
    }
}
