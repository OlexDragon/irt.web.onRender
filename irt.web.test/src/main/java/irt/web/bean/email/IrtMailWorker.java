package irt.web.bean.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

import irt.web.bean.jpa.WebContent;
import irt.web.bean.jpa.WebContentRepository;
import irt.web.controllers.OnRenderRestController.BootstapClass;
import irt.web.controllers.OnRenderRestController.ResponseMessage;
import jakarta.annotation.PostConstruct;

//@Service
public class IrtMailWorker implements MailWorker {
	private final Logger logger = LogManager.getLogger();

	@Autowired private WebContentRepository	 	webContentRepository;
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

		try {

			// Get permission
			String uri = "https://login.microsoftonline.com/" + irtEMailData.getTenantId() + "/oauth2/v2.0/token";
			HttpPost authenticationHttppost = new HttpPost(uri);
			authenticationHttppost.setHeader("Host", "login.microsoftonline.com");
			authenticationHttppost.setHeader("Content-Type", "application/x-www-form-urlencoded");

			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<NameValuePair>(2);
			params.add(new BasicNameValuePair("client_id", irtEMailData.getClientId()));
			params.add(new BasicNameValuePair("scope", graphUserScopes));
			params.add(new BasicNameValuePair("client_secret", irtEMailData.getClientSecret()));
			params.add(new BasicNameValuePair("grant_type", "client_credentials"));

			authenticationHttppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpEntity entity = execute(authenticationHttppost);

			if (entity != null) {
//				String text = EntityUtils.toString(entity);
//				logger.error(text);

				final AccessToken accessToken = new ObjectMapper().readValue(entity.getContent(), AccessToken.class);
				logger.debug(accessToken);

				// Send email
				uri = "https://graph.microsoft.com/v1.0/users/" + irtEMailData.getFrom() + "/sendMail";
				HttpPost messageHttppost = new HttpPost(uri);
				messageHttppost.setHeader("Authorization", accessToken.getAccess_token());
				messageHttppost.setHeader("Content-Type", "application/json");

				final String jSon = webEmail.toJSon("WEB Mail from " + webEmail.getFirstName(), irtEMailData.getTo());
				logger.trace(jSon);
				final StringEntity stringEntity = new StringEntity(jSon, ContentType.APPLICATION_JSON);

				messageHttppost.setEntity(stringEntity);

				entity = execute(messageHttppost);
				if (entity != null)
					return new ResponseMessage("Thank you. We have received your email.", BootstapClass.TXT_BG_SUCCESS);

				return new ResponseMessage("No response was received from the email server.", BootstapClass.TXT_BG_WARNING);
			}

			return new ResponseMessage("Failed to obtain authentication.", BootstapClass.TXT_BG_WARNING);

		} catch (IOException e) {
			logger.catching(e);

			return new ResponseMessage(e.getLocalizedMessage(), BootstapClass.TXT_BG_DANGER);
		}
	}

	private HttpEntity execute(HttpPost httppost) throws IOException, ClientProtocolException {

		HttpClient httpclient  = HttpClients.createDefault();

		//Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);
		logger.debug(response.getStatusLine());

		return response.getEntity();
	}

//	final EMailProperties eMailProperties = new EMailProperties();
//	eMailProperties.pUrl = "https://login.microsoftonline.com/" + irtEMailData.getTenantId() + "/oauth2/v2.0/token";
//	eMailProperties.pHeaders.put("Host", "login.microsoftonline.com");
//	eMailProperties.pHeaders.put("Content-Type", "application/x-www-form-urlencoded");
//	eMailProperties.pValues.put("client_id", irtEMailData.getClientId());
//	eMailProperties.pValues.put("scope", graphUserScopes);
//	eMailProperties.pValues.put("client_secret", irtEMailData.getClientSecret());
//	eMailProperties.pValues.put("grant_type", "client_credentials");
//
//	eMailProperties.url = "https://graph.microsoft.com/v1.0/users/" + irtEMailData.getFrom() + "/sendMail";
//	eMailProperties.headers.put("Content-Type", "login.microsoftonline.com");
//	eMailProperties.message = webEmail.toJSon("WEB Mail from " + webEmail.getFirstName(), irtEMailData.getTo());
//
//	return eMailProperties;
//
//	@Getter @Setter @ToString
//	public static class EMailProperties{
//
//		public String pUrl;
//		public final Map<String, String> pHeaders = new HashMap<>();
//		public final Map<String, String> pValues = new HashMap<>();
//
//		public String url;
//		public final Map<String, String> headers = new HashMap<>();
//		public String message;
//		public ResponseMessage responseMessage;
//	}
}
