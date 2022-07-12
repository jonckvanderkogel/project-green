package project.green.support;

import lombok.extern.slf4j.Slf4j;
import project.green.configuration.SecurityConfiguration;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.function.Supplier;

@Slf4j
public class SecuritySupport {
    private static final SecurityConfiguration SECURITY_CONFIGURATION = new SecurityConfiguration();

    public static Supplier<Signature> getSigningSignatureSupplier() {
        try {
            KeyStore keyStore = SECURITY_CONFIGURATION.keyStore("signingTest.p12", "PKCS12", "changeit");
            PrivateKey privateKey = SECURITY_CONFIGURATION.privateKey(keyStore, "greenSigningKey", "changeit");
            return SECURITY_CONFIGURATION.signingSignatureSupplier(privateKey);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableKeyException e) {
            log.error("Error happened while loading key store", e);
            throw new RuntimeException(e);
        }
    }

    public static Supplier<Signature> getVerificationSignatureSupplier() {
        try {
            KeyStore keyStore = SECURITY_CONFIGURATION.keyStore("signingTest.p12", "PKCS12", "changeit");
            PublicKey publicKey = SECURITY_CONFIGURATION.publicKey(keyStore, "greenSigningKey");
            return SECURITY_CONFIGURATION.verificationSignatureSupplier(publicKey);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            log.error("Error happened while loading key store", e);
            throw new RuntimeException(e);
        }
    }
}
