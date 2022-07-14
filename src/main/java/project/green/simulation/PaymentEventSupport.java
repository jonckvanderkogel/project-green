package project.green.simulation;

import com.github.javafaker.Faker;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import project.green.kafka.payments.PaymentEvent;
import project.green.kafka.payments.PaymentEventWithPerspective;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.SplittableRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static project.green.entity.Currency.EUR;

public class PaymentEventSupport {
    private static final Faker FAKER = new Faker();
    private static final SplittableRandom RANDOM = new SplittableRandom();

    public static PaymentEventWithPerspective generatePaymentEventWithPerspective(PaymentEvent origin, String perspectiveAccount) {
        return new PaymentEventWithPerspective(origin, perspectiveAccount);
    }

    public static PaymentEventWithPerspective generatePaymentEventWithPerspective(String fromAccount, String fromName) {
        return new PaymentEventWithPerspective(generatePaymentEvent(fromAccount, fromName), fromAccount);
    }

    public static PaymentEventWithPerspective generatePaymentEventWithPerspective(String fromAccount) {
        return generatePaymentEventWithPerspective(fromAccount,
            trimToMax255(FAKER.name().fullName())
        );
    }

    public static PaymentEventWithPerspective generatePaymentEventWithPerspective() {
        PaymentEvent paymentEvent = generatePaymentEvent();
        return new PaymentEventWithPerspective(paymentEvent, paymentEvent.getFromAccount());
    }

    public static PaymentEvent generatePaymentEventFromToAccount(String fromAccount, String toAccount) {
        return new PaymentEvent(
            fromAccount,
            toAccount,
            trimToMax255(FAKER.name().fullName()),
            trimToMax255(FAKER.name().fullName()),
            generateAmount(),
            EUR,
            ZonedDateTime.now(ZoneId.of("GMT+01:00")),
            trimToMax255(FAKER.dune().quote()),
            generatePaymentReference(),
            trimToMax255(FAKER.hobbit().quote())
        );
    }

    public static PaymentEvent generatePaymentEvent(String fromAccount, String fromName) {
        return new PaymentEvent(
            fromAccount,
            new Iban.Builder()
                .countryCode(CountryCode.NL)
                .bankCode("INGB")
                .buildRandom()
                .toString(),
            fromName,
            trimToMax255(FAKER.name().fullName()),
            generateAmount(),
            EUR,
            ZonedDateTime.now(ZoneId.of("GMT+01:00")),
            trimToMax255(FAKER.dune().quote()),
            generatePaymentReference(),
            trimToMax255(FAKER.hobbit().quote())
        );
    }

    public static PaymentEvent generatePaymentEvent(String fromAccount) {
        return generatePaymentEvent(fromAccount,
            trimToMax255(FAKER.name().fullName())
        );
    }

    public static PaymentEvent generatePaymentEvent() {
        return generatePaymentEvent(new Iban.Builder()
                .countryCode(CountryCode.NL)
                .bankCode("INGB")
                .buildRandom()
                .toString(),
            trimToMax255(FAKER.name().fullName())
        );
    }

    public static PaymentEvent swapFromTo(PaymentEvent origin) {
        return new PaymentEvent(
            origin.getToAccount(),
            origin.getFromAccount(),
            origin.getToName(),
            origin.getFromName(),
            origin.getValue(),
            origin.getCurrency(),
            origin.getTransactionDateTime(),
            origin.getMessage(),
            origin.getPaymentReference(),
            origin.getExtraDescription()
        );
    }

    private static Double generateAmount() {
        return RANDOM.nextDouble(1d, 1500d);
    }

    private static String generatePaymentReference() {
        return IntStream.range(0, 16)
            .mapToObj(i -> String.valueOf(RANDOM.nextInt(0, 10)))
            .collect(Collectors.joining());
    }

    private static String trimToMax255(String input) {
        return input.substring(0, Math.min(input.length(), 255));
    }
}
