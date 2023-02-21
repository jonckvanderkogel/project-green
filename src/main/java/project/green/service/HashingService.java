package project.green.service;

import lombok.RequiredArgsConstructor;
import project.green.entity.PaymentTransaction;
import project.green.kafka.payments.PaymentEventWithPerspective;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static project.green.util.DateTimeUtil.FORMATTER;

@RequiredArgsConstructor
public class HashingService {
    private final Supplier<MessageDigest> digest;

    private final static List<Function<PaymentEventWithPerspective, Byte[]>> PAYMENT_EVENT_WITH_PERSPECTIVE_FUNCTION = List.of(
            (pe) -> fromPrimByteArray(pe.getPerspectiveAccount().getBytes(StandardCharsets.UTF_8)),
            (pe) -> fromPrimByteArray(pe.getFromAccount().getBytes(StandardCharsets.UTF_8)),
            (pe) -> fromPrimByteArray(pe.getToAccount().getBytes(StandardCharsets.UTF_8)),
            (pe) -> fromPrimByteArray(pe.getFromName().getBytes(StandardCharsets.UTF_8)),
            (pe) -> fromPrimByteArray(pe.getToName().getBytes(StandardCharsets.UTF_8)),
            (pe) -> fromPrimByteArray(String.valueOf(pe.getValue()).getBytes(StandardCharsets.UTF_8)),
            (pe) -> fromPrimByteArray(pe.getCurrency().toString().getBytes(StandardCharsets.UTF_8)),
            (pe) -> fromPrimByteArray(pe.getTransactionDateTime().format(FORMATTER).getBytes(StandardCharsets.UTF_8)),
            (pe) -> fromPrimByteArray(pe.getMessage().getBytes(StandardCharsets.UTF_8)),
            (pe) -> fromPrimByteArray(pe.getPaymentReference().getBytes(StandardCharsets.UTF_8)),
            (pe) -> fromPrimByteArray(pe.getExtraDescription().getBytes(StandardCharsets.UTF_8))
    );

    private static Byte[] fromPrimByteArray(byte[] bytes) {
        Byte[] byteObjects = new Byte[bytes.length];

        int i=0;
        for (byte b : bytes)
            byteObjects[i++] = b;

        return byteObjects;
    }

    public <T> String calculateBlockHash(T event, String previousBlockHash, List<Function<T, Byte[]>> funs) {
        String concatenatedHashes = funs.stream()
                .map(fun -> hash(fun.apply(event)))
                .collect(Collectors.joining())
                .concat(previousBlockHash);

        return hash(concatenatedHashes.getBytes(StandardCharsets.UTF_8));
    }

    public String calculateBlockHash(PaymentEventWithPerspective paymentEvent, String previousBlockHash) {
        return calculateBlockHash(paymentEvent, previousBlockHash, PAYMENT_EVENT_WITH_PERSPECTIVE_FUNCTION);
    }

    public String calculateBlockHash(PaymentEventWithPerspective paymentEvent, PaymentTransaction previousPaymentTransaction) {
        return calculateBlockHash(paymentEvent, previousPaymentTransaction.getBlockHash());
    }

    public String hash(final byte[] bytes) {
        return bytesToHex(digest.get().digest(bytes));
    }

    public String hash(final Byte[] bytes) {
        byte[] bytesPrimitive = new byte[bytes.length];

        int i=0;
        for(Byte b: bytes)
            bytesPrimitive[i++] = b;

        return bytesToHex(digest.get().digest(bytesPrimitive));
    }

    /*
     * From <a href="https://www.baeldung.com/sha-256-hashing-java">Baeldung</a>
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
