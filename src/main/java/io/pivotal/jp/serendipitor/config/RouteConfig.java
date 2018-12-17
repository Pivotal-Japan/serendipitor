package io.pivotal.jp.serendipitor.config;

import io.pivotal.jp.serendipitor.oneonone.OneOnOneController;
import io.pivotal.jp.serendipitor.user.SlackUserController;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.SEE_OTHER;
import static org.springframework.web.reactive.function.server.ServerResponse.status;

@Configuration
public class RouteConfig {
	@Bean
	public RouterFunction<ServerResponse> routes(SlackUserController slackUserController,
			OneOnOneController oneOnOneController) {
		return RouterFunctions.route() //
				.GET("/", req -> status(SEE_OTHER) //
						.header(LOCATION, "/index.html") //
						.build()) //
				.add(slackUserController.routes()) //
				.add(oneOnOneController.routes()) //
				.build();
	}
}
