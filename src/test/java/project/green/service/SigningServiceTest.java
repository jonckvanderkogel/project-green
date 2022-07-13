package project.green.service;

import org.junit.jupiter.api.Test;
import project.green.support.SecuritySupport;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.util.function.Supplier;

public class SigningServiceTest {

    @Test
    public void wrongMessageSignatureShouldNotVerify() {
        Supplier<Signature> signingSignatureSupplier = SecuritySupport.getSigningSignatureSupplier();
        Supplier<Signature> verificationSignatureSupplier = SecuritySupport.getVerificationSignatureSupplier();
        SigningService signingService = new SigningService(signingSignatureSupplier, verificationSignatureSupplier);

        String message = "message";

        StepVerifier
            .create(signingService.sign(message.getBytes(StandardCharsets.UTF_8)))
            .expectNextMatches(sig -> !signingService.verify("foo".getBytes(StandardCharsets.UTF_8), sig))
            .expectComplete()
            .verify();
    }

    @Test
    public void matchingSignatureShouldVerify() {
        Supplier<Signature> signingSignatureSupplier = SecuritySupport.getSigningSignatureSupplier();
        Supplier<Signature> verificationSignatureSupplier = SecuritySupport.getVerificationSignatureSupplier();
        SigningService signingService = new SigningService(signingSignatureSupplier, verificationSignatureSupplier);

        byte[] messageBytes = "message".getBytes(StandardCharsets.UTF_8);

        StepVerifier
            .create(signingService.sign(messageBytes))
            .expectNextMatches(sig -> signingService.verify(messageBytes, sig))
            .expectComplete()
            .verify();
    }
}
