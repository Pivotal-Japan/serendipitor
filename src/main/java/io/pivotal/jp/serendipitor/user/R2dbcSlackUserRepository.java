package io.pivotal.jp.serendipitor.user;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.function.TransactionalDatabaseClient;
import org.springframework.stereotype.Repository;

@Repository
public class R2dbcSlackUserRepository implements SlackUserRepository {
	private final TransactionalDatabaseClient databaseClient;

	public R2dbcSlackUserRepository(TransactionalDatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
	}

	@Override
	public Flux<SlackUser> findAll() {
		return this.databaseClient.execute()
				.sql("SELECT user_id, user_name FROM slack_user ORDER BY user_name")
				.map((row, meta) -> new SlackUser(row.get("user_id", String.class),
						row.get("user_name", String.class))) //
				.all();
	}

	@Override
	public Mono<SlackUser> save(SlackUser slackUser) {
		return this.databaseClient.inTransaction(client -> client.execute() //
				.sql("INSERT INTO slack_user(user_id, user_name) VALUES ($1, $2)") //
				.bind("$1", slackUser.getUserId()) //
				.bind("$2", slackUser.getUserName()) //
				.fetch() //
				.rowsUpdated() //
				.thenReturn(slackUser)) //
				.single();
	}

	@Override
	public Mono<Void> delete(SlackUser slackUser) {
		return this.databaseClient.inTransaction(client -> client.execute() //
				.sql("DELETE FROM slack_user WHERE user_id = $1") //
				.bind("$1", slackUser.getUserId()) //
				.fetch() //
				.rowsUpdated() //
				.then()) //
				.single();
	}
}
