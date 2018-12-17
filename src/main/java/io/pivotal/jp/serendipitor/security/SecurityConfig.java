package io.pivotal.jp.serendipitor.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpMethod.POST;

@Component
public class SecurityConfig {
	@Bean
	ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> userReactiveOAuth2UserService() {
		return new SlackReactiveOAuth2UserService();
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.authorizeExchange() //
				.pathMatchers(POST, "/").permitAll() //
				.anyExchange().authenticated() //
				.and() //
				.csrf().disable() //
				.oauth2Login() //
				.and() //
				.build();
	}
}
