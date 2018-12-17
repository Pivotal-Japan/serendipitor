package io.pivotal.jp.serendipitor.oneonone;

import reactor.core.scheduler.Schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OneOnOneScheduler {
	private final OneOnOneChooser oneOnOneChooser;

	public OneOnOneScheduler(OneOnOneChooser oneOnOneChooser) {
		this.oneOnOneChooser = oneOnOneChooser;
	}

	@Scheduled(cron = "0 0 9 * * MON", zone = "JST")
	public void schedule() {
		this.oneOnOneChooser.choose() //
				.log() //
				.subscribeOn(Schedulers.immediate()) //
				.subscribe();
	}
}
