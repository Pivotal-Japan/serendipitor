package io.pivotal.jp.serendipitor.oneonone;

import reactor.core.publisher.Mono;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
public class OneOnOneController {
	private final OneOnOneChooser oneOnOneChooser;
	private final OneOnOneRepository oneOnOneRepository;

	public OneOnOneController(OneOnOneChooser oneOnOneChooser,
			OneOnOneRepository oneOnOneRepository) {
		this.oneOnOneChooser = oneOnOneChooser;
		this.oneOnOneRepository = oneOnOneRepository;
	}

	public RouterFunction<ServerResponse> routes() {
		return RouterFunctions.route() //
				.POST("/", this::choose) //
				.GET("/oneonones", this::history) //
				.build();
	}

	public Mono<ServerResponse> choose(ServerRequest req) {
		Mono<OneOnOne> choose = this.oneOnOneChooser.choose();
		return ServerResponse.noContent().build(choose.then());
	}

	public Mono<ServerResponse> history(ServerRequest req) {
		return ServerResponse.ok() //
				.body(this.oneOnOneRepository.findAll(), OneOnOne.class);
	}
}
