package io.pivotal.jp.serendipitor.oneonone;

import java.time.LocalDate;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.function.TransactionalDatabaseClient;
import org.springframework.stereotype.Repository;

@Repository
public class R2dbcOneOnOneRepository implements OneOnOneRepository {
	private final TransactionalDatabaseClient databaseClient;

	public R2dbcOneOnOneRepository(TransactionalDatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
	}

	@Override
	public Mono<OneOnOne> save(OneOnOne oneOnOne) {
		return this.databaseClient.inTransaction(client -> client.execute() //
				.sql("INSERT INTO one_on_one(first, second, date) VALUES ($1, $2, $3)") //
				.bind("$1", oneOnOne.getFirst()) //
				.bind("$2", oneOnOne.getSecond()) //
				.bind("$3", oneOnOne.getDate()) //
				.fetch() //
				.rowsUpdated() //
				.thenReturn(oneOnOne)) //
				.single();
	}

	@Override
	public Flux<OneOnOne> findAll() {
		return this.databaseClient.execute()
				.sql("SELECT first, second, date FROM one_on_one ORDER BY date DESC")
				.map((row, meta) -> new OneOnOne(row.get("first", String.class),
						row.get("second", String.class),
						row.get("date", LocalDate.class))) //
				.all();
	}
}
