package irt.web.bean.email;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import irt.web.controllers.OnRenderRestController.BootstapClass;
import irt.web.controllers.OnRenderRestController.ResponseMessage;

@Service
public class CurlMailWorker implements MailWorker {
	private final static Logger logger = LogManager.getLogger();
	
	private final String tokenUrl	 = "https://login.microsoftonline.com/%s/oauth2/v2.0/token";
	private final String urlToSend	 = "https://graph.microsoft.com/v1.0/users/%s/sendMail";
	private final String data		 = "client_id=%s&scope=%s&client_secret=%s&grant_type=client_credentials";

	@Value("${app.graphUserScopes}")
	private String graphUserScopes;

	@Override
	public ResponseMessage sendEmail(final WebEmail webEmail, final IrtEMailData irtEMailData) {
		logger.info("\n{}\n{}", webEmail, irtEMailData);

		try {

			String authorization;

			// Authentication

			final String tenantId = irtEMailData.getTenantId();
			final String url = String.format(tokenUrl, tenantId);
			final String d = String.format(data, irtEMailData.getClientId(), URLEncoder.encode(graphUserScopes, "UTF-8"), irtEMailData.getClientSecret());
			logger.debug(d);
			
			final Process process = new ProcessBuilder("curl", "--header", "Content-Type:application/x-www-form-urlencoded", "-d", d, url).start();
			logger.debug("{} {} {} {} {} {}", "curl", "--header", "Content-Type:application/x-www-form-urlencoded", "-d", d, url);

			try(	final InputStream is = process.getInputStream();
					final InputStreamReader in = new InputStreamReader(is, StandardCharsets.UTF_8);
					final BufferedReader bufferedReader = new BufferedReader(in);){

				String text = bufferedReader.lines().collect(Collectors.joining("\n"));

				logger.debug(text);
				if(text.contains("error")) {
					final EMailServerError value = new ObjectMapper().readValue(text, EMailServerError.class);
					logger.warn(value);
					return  new ResponseMessage("Problems with the mail server Authorization. Please try again later.", BootstapClass.TXT_BG_DANGER);
				}
				final AccessToken accessToken = new ObjectMapper().readValue(text, AccessToken.class);

				process.destroy();

				Optional<String> oBearer = Optional.ofNullable(accessToken).map(AccessToken::getAccess_token).filter(at->at!=null);
				if(!oBearer.isPresent())
					return  new ResponseMessage("Problems with the mail server. Please try again later.", BootstapClass.TXT_BG_DANGER);

				authorization = oBearer.get();
			}

			// Send email
			final String from = irtEMailData.getFrom();
			final String u = String.format(urlToSend, from);
			final String data = webEmail.toMimeHtml("WEB Mail from " + webEmail.getFirstName(), irtEMailData.getTo());
			logger.debug(data);

			final String[] command = new String[] {"curl", "--header", "Content-Type:text/plain", "--header", "Authorization:" + authorization, "-d", data, "-v", u};
			logger.debug("{}", ()->Arrays.stream(command).collect(Collectors.joining(" ")));

			final ProcessBuilder processBuilder = new ProcessBuilder(command);

			processBuilder.redirectErrorStream(true);
			final Process p = processBuilder.start();

			String headerAndBody;
			try( 	final InputStream is = p.getInputStream();
					final InputStreamReader in = new InputStreamReader(is, StandardCharsets.UTF_8);
					final BufferedReader bufferedReader = new BufferedReader(in);){

				headerAndBody = bufferedReader.lines().collect(Collectors.joining("\n"));
				logger.debug(headerAndBody);

				p.destroy();

				if(headerAndBody.contains("202 Accepted") || headerAndBody.contains("HTTP/2 202"))
					return new ResponseMessage("Thank you. We have received your email.", BootstapClass.TXT_BG_SUCCESS);
			}

			logger.warn(headerAndBody);
			return  new ResponseMessage("Problems with the mail server. Please try again later.", BootstapClass.TXT_BG_DANGER);

		} catch (Exception e) {
			logger.catching(e);
			return new ResponseMessage(e.getLocalizedMessage(), BootstapClass.TXT_BG_DANGER);
		}
	}
}
