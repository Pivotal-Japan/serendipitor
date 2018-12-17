package io.pivotal.jp.serendipitor.slack;

import reactor.core.publisher.Mono;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@ConfigurationProperties(prefix = "slack")
public class SlackNotifier {
	private WebClient webClient = WebClient.create();
	private String webhookUrl;

	public Mono<Void> notify(Payload payload) {
		return this.webClient.post() //
				.uri(this.getWebhookUrl()) //
				.syncBody(payload) //
				.exchange() //
				.then();
	}

	public String getWebhookUrl() {
		return webhookUrl;
	}

	public void setWebhookUrl(String webhookUrl) {
		this.webhookUrl = webhookUrl;
	}
}
