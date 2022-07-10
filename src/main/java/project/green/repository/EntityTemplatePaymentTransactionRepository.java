package project.green.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

@RequiredArgsConstructor
public class EntityTemplatePaymentTransactionRepository {
    private final R2dbcEntityTemplate template;

    /*
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
FROM payment_transaction
WHERE (from_account='NL30INGB3130212954' OR to_account='NL30INGB3130212954') AND id >
(SELECT transaction_id FROM public.offset WHERE account='NL30INGB3130212954')
     */
}
