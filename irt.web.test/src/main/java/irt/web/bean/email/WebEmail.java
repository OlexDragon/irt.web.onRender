package irt.web.bean.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.web.bean.IpData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor @Getter @Setter @ToString
public class WebEmail {
	private final static Logger logger = LogManager.getLogger();

	private String firstName;
	private String lastName;
	private String phone;
	private String email;
	private String company;
	private String industry;
	private String message;

	@NonNull
	private Optional<IpData> ipData = Optional.empty();

	public String toHtml() throws IOException {

		final InputStream resourceAsStream = getClass().getResourceAsStream("/static/webEmail.html");
		final String toSend = Optional.ofNullable(message).map(m->m.split("\n")).map(Arrays::stream).map(s->s.collect(Collectors.joining("<br/>"))).orElse("");
		try(final InputStreamReader is = new InputStreamReader(resourceAsStream);){

			String result;
			String ipD;
			result = new BufferedReader(is).lines().collect(Collectors.joining());
			ipD = ipData.map(
					t -> {
							try {

								return t.toHtml();

							} catch (IOException e) {
								logger.catching(e);
								return "";
							}
						}).orElse("");

			return String.format(result, firstName, lastName, phone, email, company, industry, toSend, ipD);
		}
	}

	public String getText() {
		StringBuilder sb = new StringBuilder();

		sb.append("First Name: ").append(firstName).append("<br/>");
		sb.append("Last Name: ").append(lastName).append("<br/>");
		sb.append("Phone: ").append(phone).append("<br/>");
		sb.append("Email: ").append(email).append("<br/>");
		sb.append("Company: ").append(company).append("<br/>");
		sb.append("industry: ").append(industry).append("<br/><br/>");
		sb.append(message).append("<br/>");

		return sb.toString();
	}

	public String toMimeHtml(String subject, String to) throws IOException {

		final StringBuilder sb = new StringBuilder();
		sb.append("MIME-Version: 1.0").append('\n');
		sb.append("To:").append(to).append('\n');
		sb.append("Subject:").append(subject).append('\n');
		sb.append("Accept-Language: en-US").append('\n');
		sb.append("Content-Language: en-US").append('\n');
		sb.append("Content-Type: text/html; charset=UTF-8").append('\n');
		sb.append("Content-Disposition: inline").append('\n');
		sb.append("Content-Transfer-Encoding: quoted-printable").append("\n\n");
		sb.append(toHtml()).append('\n');

		final String string = sb.toString();

		return Base64.getMimeEncoder().encodeToString(string.getBytes());
	}

	public String toMimeText(String subject, String to) {

		final StringBuilder sb = new StringBuilder();
		sb.append("MIME-Version: 1.0").append('\n');
		sb.append("To:").append(to).append('\n');
		sb.append("Subject:").append(subject).append('\n');
		sb.append("Accept-Language: en-US").append('\n');
		sb.append("Content-Language: en-US").append('\n');
		sb.append("Content-Type: text/plain; charset=UTF-8").append('\n');
		sb.append("Content-Disposition: inline").append('\n');
		sb.append("Content-Transfer-Encoding: quoted-printable").append("\n\n");
		sb.append(getText()).append('\n');

		final String string = sb.toString();

		return Base64.getMimeEncoder().encodeToString(string.getBytes());
	}

	public String toJSon(String subject, String to) throws IOException {

		final String html = toHtml();
		final Encoder mimeEncoder = Base64.getMimeEncoder();
		final String jSon = toJSon(subject, to, html);

		return '\"' + mimeEncoder.encodeToString(jSon.getBytes()) + '\"';
	}

	public String toJSon(String subject, String to, final String html) {

		return "{"
				+ " \"message\" : {"
					+ " \"subject\" : \"" + subject + "\","
					+ " \"body\" : {"
						+ " \"contentType\" : \"HTML\","
						+ " \"content\" : \"" + html + "\""
					+ "},"
					+ "\"toRecipients\" : ["
						+ "{"
							+ "\"emailAddress\" : {"
								+ "\"address\" : \"" + to + "\""
							+ "}"
						+ "}"
					+ "]"
				+ "},"
				+ "\"saveToSentItems\" : \"false\""
			+ "}";
	}

	public WebEmailRequestBody toWebEmailRequestBody(String subject, String to) throws IOException {

		List<WebEmailAddress> emails = new ArrayList<>();
		emails.add(
				new WebEmailAddress(
						new EMail(to)));

		final WebEmailBody body = new WebEmailBody();
		body.setContentType("HTML");
		body.setContent(toHtml());

		final WebEmailMessage webEmailMessage = new WebEmailMessage();
		webEmailMessage.setSubject(subject);
		webEmailMessage.setBody(body);
		webEmailMessage.setToRecipients(emails);

		final WebEmailRequestBody webEmailRequestBody = new WebEmailRequestBody(webEmailMessage, false);

		return webEmailRequestBody;
	}

	@RequiredArgsConstructor @Getter @ToString
	public static class WebEmailRequestBody{
		private final WebEmailMessage message;
		private final Boolean saveToSentItems;
	}

	@NoArgsConstructor @Getter @Setter @ToString
	public static class WebEmailMessage{
		private String subject;
		private WebEmailBody body;
		private List<WebEmailAddress> toRecipients;
	}

	@NoArgsConstructor @Getter @Setter @ToString
	public class WebEmailBody {
		private String contentType;
		private String content;
	}

	@RequiredArgsConstructor @Getter @ToString
	public class WebEmailAddress {
		private final EMail emailAddress;
	}

	@RequiredArgsConstructor @Getter @ToString
	public class EMail {
		private final String address;
	}
}
