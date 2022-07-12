## Project Green

### Running the project
First start up Docker Compose (from the root of the project):
```
docker compose up
```

Set the KEYSTORE_PASSWORD environment variable in your terminal session:
```
export KEYSTORE_PASSWORD=the_keystore_password
```

From the root of the project, start up the application:
```
mvn spring-boot:run
```

You will now start to see messages being produced and pushed to Kafka. These messages are then in turn picked up from
the Kafka queue and persisted in Postgres. You can now query for the transactions of a certain account by performing
a GET request:
```
curl "http://localhost:8080/transactions?account=NL53INGB1464897916"
```
This will return all the messages up to that point and place an offset marker in the database. So any consecutive calls
will only fetch any new transactions that might have been generated. Look in project.green.simulation.SimulationProvider
to see for which IBAN nrs the simulation is running for.

### Creating signing.p12
In case you don't have the KEYSTORE_PASSWORD, you can either use the keystore from test/resources or create your own.

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

Don't forget to update the properties in keystore.yml. 
