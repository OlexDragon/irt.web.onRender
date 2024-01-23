package irt.web.bean.email;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor @Getter @Setter @ToString
public class WebEmail {

	private String firstName;
	private String lastName;
	private String phone;
	private String email;
	private String company;
	private String industry;
	private String message;

	public String getHtml() {
		final InputStream resourceAsStream = getClass().getResourceAsStream("/static/webEmail.html");
		String result = new BufferedReader(new InputStreamReader(resourceAsStream)).lines().collect(Collectors.joining("\n"));
		return String.format(result, firstName, lastName, phone, email, company, industry, message);
	}
}
