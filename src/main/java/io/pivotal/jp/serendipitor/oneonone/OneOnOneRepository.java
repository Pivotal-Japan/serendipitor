package io.pivotal.jp.serendipitor.oneonone;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OneOnOneRepository {
	Mono<OneOnOne> save(OneOnOne oneOnOne);

	Flux<OneOnOne> findAll();
}
