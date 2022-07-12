package project.green.service;

import org.junit.jupiter.api.Test;
import project.green.support.SecuritySupport;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.SignatureException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SigningServiceTest {

    @Test
    public void wrongMessageSignatureShouldNotVerify() {
        Supplier<Signature> signingSignatureSupplier = SecuritySupport.getSigningSignatureSupplier();
        Supplier<Signature> verificationSignatureSupplier = SecuritySupport.getVerificationSignatureSupplier();
        SigningService signingService = new SigningService(signingSignatureSupplier, verificationSignatureSupplier);

        String message = "message";

        String encodedSignature = signingService.sign(message.getBytes(StandardCharsets.UTF_8));
        assertFalse(signingService.verify("foo".getBytes(StandardCharsets.UTF_8), encodedSignature));
    }

    @Test
    public void matchingSignatureShouldVerify() {
        Supplier<Signature> signingSignatureSupplier = SecuritySupport.getSigningSignatureSupplier();
        Supplier<Signature> verificationSignatureSupplier = SecuritySupport.getVerificationSignatureSupplier();
        SigningService signingService = new SigningService(signingSignatureSupplier, verificationSignatureSupplier);

        byte[] messageBytes = "message".getBytes(StandardCharsets.UTF_8);

        String encodedSignature = signingService.sign(messageBytes);
        assertTrue(signingService.verify(messageBytes, encodedSignature));
    }
}
