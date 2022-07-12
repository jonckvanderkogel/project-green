### Project Green

## Creating signing.p12
Create JKS:
```
keytool -genkey -alias greenSigningKey -keyalg RSA -keystore signing.jks -validity 1825
```

Convert to P12:
```
keytool -importkeystore -srckeystore signing.jks -destkeystore signing.p12 -srcstoretype JKS -deststoretype PKCS12
```

List private key/certificate:
```
openssl pkcs12 -info -in signing.p12
```
