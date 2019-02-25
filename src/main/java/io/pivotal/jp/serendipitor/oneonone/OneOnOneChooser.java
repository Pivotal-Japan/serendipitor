package io.pivotal.jp.serendipitor.oneonone;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import io.pivotal.jp.serendipitor.slack.Action;
import io.pivotal.jp.serendipitor.slack.Attachment;
import io.pivotal.jp.serendipitor.slack.Payload;
import io.pivotal.jp.serendipitor.slack.SlackNotifier;
import io.pivotal.jp.serendipitor.user.SlackUserRepository;
import reactor.core.publisher.Mono;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import static java.util.function.Predicate.not;

@Component
@ConfigurationProperties(prefix = "oneonone")
public class OneOnOneChooser {
	private final SlackUserRepository slackUserRepository;
	private final SlackNotifier slackNotifier;
	private final OneOnOneRepository oneOnOneRepository;
	private String channel;

	public OneOnOneChooser(SlackUserRepository slackUserRepository,
			SlackNotifier slackNotifier, OneOnOneRepository oneOnOneRepository) {
		this.slackUserRepository = slackUserRepository;
		this.slackNotifier = slackNotifier;
		this.oneOnOneRepository = oneOnOneRepository;
	}

	public Mono<OneOnOne> choose() {
		return this.slackUserRepository.findAll() //
				.collectList() //
				.filter(not(List::isEmpty)) //
				.map(list -> {
					if (list.size() == 1) {
						return new OneOnOne(list.get(0).getUserId(),
								list.get(0).getUserId(), LocalDate.now());
					}
					Collections.shuffle(list);
					return new OneOnOne(list.get(0).getUserId(), list.get(1).getUserId(),
							LocalDate.now());
				}) //
				.flatMap(oneOnOne -> {
					Payload payload = new Payload();
					payload.setChannel(this.getChannel());
					payload.setText(
							String.format("今週の1on1は<@%s>と<@%s>です。%sから%sまでの間に実施しましょう。",
									oneOnOne.getFirst(), oneOnOne.getSecond(),
									oneOnOne.getDate(), oneOnOne.getDate().plusDays(5)));
					payload.setAttachments(List.of(new Attachment() {

						{
							setFallback("Failed");
							setCallbackId("oneonone");
							setAttachmentType("default");
							setActions(List.of(new Action() {

								{
									setName("one_more_pair");
									setText("もう1組");
									setType("button");
									setValue("one_more_pair");
								}
							}));
						}
					}));
					return this.slackNotifier.notify(payload) //
							.thenReturn(oneOnOne);
				}) //
				.flatMap(oneOnOne -> this.oneOnOneRepository.save(oneOnOne)
						.onErrorReturn(DuplicateKeyException.class, oneOnOne));
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
}
