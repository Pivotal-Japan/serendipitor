package io.pivotal.jp.serendipitor.user;

import java.net.URI;
import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
public class SlackUserController {
	private final SlackUserRepository slackUserRepository;

	public SlackUserController(SlackUserRepository slackUserRepository) {
		this.slackUserRepository = slackUserRepository;
	}

	public RouterFunction<ServerResponse> routes() {
		return RouterFunctions.route() //
				.GET("/slackusers", this::findAll) //
				.GET("/me", this::redirect) //
				.POST("/me", this::addMe) //
				.DELETE("/me", this::deleteMe) //
				.build();
	}

	Mono<OAuth2User> oauth2User(ServerRequest req) {
		return req.principal().cast(OAuth2AuthenticationToken.class)
				.map(OAuth2AuthenticationToken::getPrincipal);
	}

	SlackUser toSlackUser(OAuth2User oAuth2User) {
		Map<String, Object> attributes = oAuth2User.getAttributes();
		return new SlackUser((String) attributes.get("userId"),
				(String) attributes.get("userName"));
	}

	public Mono<ServerResponse> findAll(ServerRequest req) {
		Flux<SlackUser> all = this.slackUserRepository.findAll();
		return ServerResponse.ok() //
				.body(all, SlackUser.class);
	}

	public Mono<ServerResponse> redirect(ServerRequest req) {
		Mono<SlackUser> userMono = this.oauth2User(req) //
				.map(this::toSlackUser).flatMap(this.slackUserRepository::save);
		return userMono.then(ServerResponse
				.seeOther(URI.create("http://localhost:8080/slackusers")).build());
	}

	public Mono<ServerResponse> addMe(ServerRequest req) {
		Mono<SlackUser> userMono = this.oauth2User(req) //
				.map(this::toSlackUser).flatMap(this.slackUserRepository::save);
		return ServerResponse.ok() //
				.body(userMono, SlackUser.class);
	}

	public Mono<ServerResponse> deleteMe(ServerRequest req) {
		return this.oauth2User(req) //
				.map(this::toSlackUser) //
				.flatMap(this.slackUserRepository::delete)
				.flatMap(__ -> ServerResponse.noContent().build());
	}
}
