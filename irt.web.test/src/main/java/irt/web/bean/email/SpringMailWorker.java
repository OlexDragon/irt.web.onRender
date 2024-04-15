package irt.web.bean.email;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import irt.web.bean.email.WebEmail.WebEmailRequestBody;
import irt.web.bean.jpa.WebContent;
import irt.web.bean.jpa.WebContentRepository;
import irt.web.controllers.OnRenderRestController.BootstapClass;
import irt.web.controllers.OnRenderRestController.ResponseMessage;

//@Service
public class SpringMailWorker implements MailWorker {
	private final Logger logger = LogManager.getLogger();

	@Autowired private WebContentRepository	 		webContentRepository;
	@Autowired private ReactorClientHttpConnector 	connector;

	@Value("${app.graphUserScopes}")
	private String graphUserScopes;

	private IrtEMailData irtEMailData;

    @PostConstruct
	public void GraphMailWorkerInit() {

   		final List<WebContent> webContents = webContentRepository.findByPageName("email");
		irtEMailData = new IrtEMailData(webContents);
    }

	@Override
	public ResponseMessage sendEmail(WebEmail webEmail) {
		logger.traceEntry("{}", webEmail);

		final String tenantId = irtEMailData.getTenantId();
		final String uri = "https://login.microsoftonline.com/{tenantId}/oauth2/v2.0/token";

		final AccessToken accessToken = WebClient.builder()

				.clientConnector(connector)
				.build()

				.post()
				.uri(uri, tenantId)
				.header("Host", "login.microsoftonline.com")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.body(
						BodyInserters
						.fromFormData("client_id", irtEMailData.getClientId())
						.with("scope", graphUserScopes)
						.with("client_secret", irtEMailData.getClientSecret())
						.with("grant_type", "client_credentials"))
				.retrieve()
				.onStatus(HttpStatus::isError, response -> response.bodyToMono(String.class).map(Exception::new))
				.bodyToMono(AccessToken.class)
				.block();

		logger.debug(accessToken);

		if (accessToken != null) {
				logger.debug(accessToken);

				final String from = irtEMailData.getFrom();
				final WebEmailRequestBody body = webEmail.toWebEmailRequestBody("WEB Mail from " + webEmail.getFirstName(), irtEMailData.getTo());
				logger.trace(body);

				final String uriToSend = "https://graph.microsoft.com/v1.0/users/{from}/sendMail";

				AtomicBoolean sent = new AtomicBoolean();
				// Send email
				WebClient.create()
				.post()
				.uri(uriToSend, from)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", accessToken.getAccess_token())
				.body(BodyInserters.fromValue(body))
				.retrieve()
				.onStatus(HttpStatus::is2xxSuccessful, response -> { sent.set(true); return response.bodyToMono(String.class).map(Exception::new);})
				.onStatus(HttpStatus::isError, response -> response.bodyToMono(String.class).map(Exception::new))
				.bodyToMono(Void.class)
				.block();

				if (sent.get())
					return new ResponseMessage("Thank you. We have received your email.", BootstapClass.TXT_BG_SUCCESS);

			return new ResponseMessage("No response was received from the email server.", BootstapClass.TXT_BG_WARNING);
		}

		return new ResponseMessage("Failed to obtain authentication.", BootstapClass.TXT_BG_WARNING);
	}
}
