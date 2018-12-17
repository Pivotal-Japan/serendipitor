package io.pivotal.jp.serendipitor.user;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SlackUserRepository {
	Flux<SlackUser> findAll();

	Mono<SlackUser> save(SlackUser slackUser);

	Mono<Void> delete(SlackUser slackUser);
}
