package irt.web.bean;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.format.Formatter;

public class DateFormatter implements Formatter<LocalDateTime> {

	@Override
	public String print(LocalDateTime dateTime, Locale locale) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy kk:mm");
		return dateTime.format(formatter);
	}

	@Override
	public LocalDateTime parse(String text, Locale locale) throws ParseException {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy kk:mm");
		return LocalDateTime.parse(text, formatter);
	}

}
