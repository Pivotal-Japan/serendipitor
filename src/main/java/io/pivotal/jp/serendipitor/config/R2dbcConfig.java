package io.pivotal.jp.serendipitor.config;

import java.net.URI;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.function.TransactionalDatabaseClient;
import org.springframework.util.StringUtils;

@Configuration
public class R2dbcConfig {
	@Bean
	public ConnectionFactory connectionFactory(
			@Value("${vcap.services.serendipitor-db.credentials.uri:}") String envUri,
			@Value("${spring.datasource.url:}") String uriString,
			@Value("${spring.datasource.username:}") String username,
			@Value("${spring.datasource.password:}") String password) {
		URI uri;
		if (StringUtils.hasText(envUri)) {
			uri = URI.create(envUri);
			String[] userInfo = uri.getUserInfo().split(":");
			username = userInfo[0];
			password = userInfo[1];
		}
		else {
			uri = URI.create(uriString.replace("jdbc:", ""));
		}
		return new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder() //
				.host(uri.getHost()) //
				.port(uri.getPort()) //
				.username(username) //
				.password(password) //
				.database(uri.getPath().replace("/", "")) //
				.build());
	}

	@Bean
	public TransactionalDatabaseClient databaseClient(
			ConnectionFactory connectionFactory) {
		return TransactionalDatabaseClient.create(connectionFactory);
	}
}