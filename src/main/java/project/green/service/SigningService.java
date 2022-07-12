package project.green.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.function.Supplier;

@Service
public class SigningService {
    private final Supplier<Signature> signingSignatureSupplier;
    private final Supplier<Signature> verificationSignatureSupplier;
    private final static Base64.Encoder ENCODER = Base64.getEncoder();
    private final static Base64.Decoder DECODER = Base64.getDecoder();

    public SigningService(
        @Autowired @Qualifier("signingSignatureSupplier") Supplier<Signature> signingSignatureSupplier,
        @Autowired @Qualifier("verificationSignatureSupplier") Supplier<Signature> verificationSignatureSupplier
    ) {
        this.signingSignatureSupplier = signingSignatureSupplier;
        this.verificationSignatureSupplier = verificationSignatureSupplier;
    }

    public String sign(byte[] message) {
        Signature signature = signingSignatureSupplier.get();
        try {
            signature.update(message);
            return ENCODER.encodeToString(signature.sign());
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verify(byte[] message, String encodedSignature) {
        byte[] decodedSignature = DECODER.decode(encodedSignature);
        Signature signature = verificationSignatureSupplier.get();
        try {
            signature.update(message);
            return signature.verify(decodedSignature);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
    }
}
