package io.pivotal.jp.serendipitor.user;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SlackUser {
	private final String userId;
	private final String userName;

	public SlackUser(String userId, String userName) {
		this.userId = userId;
		this.userName = userName;
	}

	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	@Override
	public String toString() {
		return this.userName;
	}
}
