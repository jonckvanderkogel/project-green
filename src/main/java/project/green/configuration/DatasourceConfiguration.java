package project.green.configuration;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import project.green.util.YamlPropertySourceFactory;

@Slf4j
@Setter
@Configuration
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:datasource.yml")
@ConfigurationProperties(prefix = "datasource")
public class DatasourceConfiguration extends AbstractR2dbcConfiguration {
    public static final String CONNECTION_FACTORY = "postgresConnectionFactory";

    private String host;
    private int port;
    private String username;
    private String password;
    private String database;

    @Bean(name = CONNECTION_FACTORY)
    @Override
    public ConnectionFactory connectionFactory() {
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                .host(host)
                .port(port)
                .username(username)
                .password(password)
                .database(database)
                .build()
        );
    }
}
