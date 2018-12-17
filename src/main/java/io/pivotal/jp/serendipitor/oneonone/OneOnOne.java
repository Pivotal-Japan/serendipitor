package io.pivotal.jp.serendipitor.oneonone;

import java.time.LocalDate;

public class OneOnOne {
	private final String first;
	private final String second;
	private final LocalDate date;

	public OneOnOne(String first, String second, LocalDate date) {
		this.first = first;
		this.second = second;
		this.date = date;
	}

	public String getFirst() {
		return first;
	}

	public String getSecond() {
		return second;
	}

	public LocalDate getDate() {
		return date;
	}
}
