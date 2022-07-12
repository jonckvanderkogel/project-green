package project.green.configuration;

import project.green.service.HashingService;
import project.green.util.YamlPropertySourceFactory;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.function.Supplier;

@Setter
@Configuration
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:keystore.yml")
@ConfigurationProperties(prefix = "keystore")
public class SecurityConfiguration {
    private String file;
    private String type;
    private String alias;
    private String password;

    @Bean(value = "keystoreFile")
    public String keystore() {
        return file;
    }

    @Bean(value = "keystoreType")
    public String keystoreType() {
        return type;
    }

    @Bean(value = "keyAlias")
    public String keyAlias() {
        return alias;
    }

    @Bean(value = "keystorePassword")
    public String keystorePassword() {
        return password;
    }

    /**
     * MessageDigest is not thread safe, so use a new instance everywhere you need it.
     *
     * @return Supplier<MessageDigest>
     */
    @Bean
    public Supplier<MessageDigest> messageDigestSupplier() {
        return () -> {
            try {
                return MessageDigest.getInstance("SHA3-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Bean(name = "signingSignatureSupplier")
    public Supplier<Signature> signingSignatureSupplier(@Autowired PrivateKey privateKey) {
        return () -> {
            try {
                Signature signature = Signature.getInstance("SHA256WithRSA");
                signature.initSign(privateKey);
                return signature;
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Bean(name = "verificationSignatureSupplier")
    public Supplier<Signature> verificationSignatureSupplier(@Autowired PublicKey publicKey) {
        return () -> {
            try {
                Signature signature = Signature.getInstance("SHA256WithRSA");
                signature.initVerify(publicKey);
                return signature;
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Bean
    public HashingService hashingService(@Autowired Supplier<MessageDigest> messageDigestSupplier) {
        return new HashingService(messageDigestSupplier);
    }

    @Bean
    public KeyStore keyStore(@Autowired @Qualifier("keystoreFile") String keystoreFile,
                             @Autowired @Qualifier("keystoreType") String keystoreType,
                             @Autowired @Qualifier("keystorePassword") String keystorePassword
    ) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance(keystoreType);
        keyStore.load(new ClassPathResource(keystoreFile).getInputStream(), keystorePassword.toCharArray());

        return keyStore;
    }

    @Bean
    public PrivateKey privateKey(@Autowired KeyStore keyStore,
                                 @Autowired @Qualifier("keyAlias") String keyAlias,
                                 @Autowired @Qualifier("keystorePassword") String keystorePassword
    ) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        return (PrivateKey) keyStore.getKey(keyAlias, keystorePassword.toCharArray());
    }

    @Bean
    public PublicKey publicKey(@Autowired KeyStore keyStore,
                               @Autowired @Qualifier("keyAlias") String keyAlias
    ) throws KeyStoreException {
        return keyStore.getCertificate(keyAlias).getPublicKey();
    }
}
