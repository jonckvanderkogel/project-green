package project.green.service;

import lombok.RequiredArgsConstructor;

import java.security.MessageDigest;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class HashingService {
    private final Supplier<MessageDigest> digest;

    public String hash(final byte[] bytes) {
        return bytesToHex(digest.get().digest(bytes));
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
