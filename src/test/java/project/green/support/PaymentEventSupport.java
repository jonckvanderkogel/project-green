package project.green.support;

import com.github.javafaker.Faker;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import project.green.kafka.payments.PaymentEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static project.green.domain.Currency.EUR;

public class PaymentEventSupport {
    private static final Faker FAKER = new Faker();

    public static PaymentEvent generatePaymentEvent() {
        return new PaymentEvent(
            new Iban.Builder()
                .countryCode(CountryCode.NL)
                .bankCode("INGB")
                .buildRandom()
                .toString(),
            new Iban.Builder()
                .countryCode(CountryCode.NL)
                .bankCode("INGB")
                .buildRandom()
                .toString(),
            trimToMax255(FAKER.name().fullName()),
            trimToMax255(FAKER.name().fullName()),
            100d,
            EUR,
            ZonedDateTime.of(LocalDateTime.of(0, 1, 1, 0, 0), ZoneId.of("GMT+01:00")),
            trimToMax255(FAKER.dune().quote()),
            "12345",
            trimToMax255(FAKER.hobbit().quote())
        );
    }

    private static String trimToMax255(String input) {
        return input.substring(0, Math.min(input.length(), 255));
    }
}
