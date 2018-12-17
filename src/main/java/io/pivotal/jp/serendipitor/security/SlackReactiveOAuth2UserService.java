package io.pivotal.jp.serendipitor.security;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import net.minidev.json.JSONObject;
import reactor.core.publisher.Mono;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class SlackReactiveOAuth2UserService
		implements ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {
	private static final String INVALID_USER_INFO_RESPONSE_ERROR_CODE = "invalid_user_info_response";
	private WebClient webClient = WebClient.create();

	@Override
	public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest)
			throws OAuth2AuthenticationException {
		return Mono.defer(() -> {
			Assert.notNull(userRequest, "userRequest cannot be null");
			String userInfoUri = userRequest.getClientRegistration().getProviderDetails()
					.getUserInfoEndpoint().getUri();
			String userNameAttributeName = userRequest.getClientRegistration()
					.getProviderDetails().getUserInfoEndpoint()
					.getUserNameAttributeName();
			Mono<SlackUserResponse> userAttributes = this.webClient.get().uri(userInfoUri)
					.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
					.headers(headers -> headers
							.setBearerAuth(userRequest.getAccessToken().getTokenValue()))
					.retrieve().onStatus(s -> s != HttpStatus.OK,
							response -> parse(response).map(userInfoErrorResponse -> {
								String description = userInfoErrorResponse
										.getErrorObject().getDescription();
								OAuth2Error oauth2Error = new OAuth2Error(
										INVALID_USER_INFO_RESPONSE_ERROR_CODE,
										description, null);
								throw new OAuth2AuthenticationException(oauth2Error,
										oauth2Error.toString());
							}))
					.bodyToMono(SlackUserResponse.class);

			return userAttributes.map(attrs -> {
				Map<String, Object> attributes = Map.of( //
						"userName", attrs.getUser().getName(), //
						"userId", attrs.getUser().getId(), //
						"teamId", attrs.getTeam().getId());
				GrantedAuthority authority = new OAuth2UserAuthority(attributes);
				Set<GrantedAuthority> authorities = new HashSet<>();
				authorities.add(authority);
				return new DefaultOAuth2User(authorities, attributes,
						userNameAttributeName);
			}).onErrorMap(UnknownHostException.class,
					t -> new AuthenticationServiceException(
							"Unable to access the userInfoEndpoint " + userInfoUri, t))
					.onErrorMap(t -> !(t instanceof AuthenticationServiceException),
							t -> {
								OAuth2Error oauth2Error = new OAuth2Error(
										INVALID_USER_INFO_RESPONSE_ERROR_CODE,
										"An error occurred reading the UserInfo Success response: "
												+ t.getMessage(),
										null);
								return new OAuth2AuthenticationException(oauth2Error,
										oauth2Error.toString(), t);
							});
		});
	}

	private static Mono<UserInfoErrorResponse> parse(ClientResponse httpResponse) {
		String wwwAuth = httpResponse.headers().asHttpHeaders()
				.getFirst(HttpHeaders.WWW_AUTHENTICATE);
		if (!StringUtils.isEmpty(wwwAuth)) {
			// Bearer token error?
			return Mono.fromCallable(() -> UserInfoErrorResponse.parse(wwwAuth));
		}
		ParameterizedTypeReference<Map<String, String>> typeReference = new ParameterizedTypeReference<Map<String, String>>() {
		};
		// Other error?
		return httpResponse.bodyToMono(typeReference)
				.map(body -> new UserInfoErrorResponse(
						ErrorObject.parse(new JSONObject(body))));
	}

	public static class SlackUserResponse {
		private SlackUserInfo user;
		private SlackTeamInfo team;

		public SlackUserInfo getUser() {
			return user;
		}

		public void setUser(SlackUserInfo user) {
			this.user = user;
		}

		public SlackTeamInfo getTeam() {
			return team;
		}

		public void setTeam(SlackTeamInfo team) {
			this.team = team;
		}
	}

	public static class SlackUserInfo {
		private String id;
		private String name;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class SlackTeamInfo {
		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}
}
