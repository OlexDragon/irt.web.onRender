package irt.web;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonValue;
import com.microsoft.graph.models.BodyType;

import irt.web.bean.email.IrtEMailData;
import irt.web.bean.email.MailWorker;
import irt.web.bean.email.WebEmail;
import irt.web.bean.jpa.RemoteAddress;
import irt.web.bean.jpa.RemoteAddress.TrustStatus;
import irt.web.bean.jpa.RemoteAddressRepository;
import irt.web.bean.jpa.VariableContent;
import irt.web.bean.jpa.WebContent;
import irt.web.bean.jpa.WebContentRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@RestController
@RequestMapping("rest")
public class IrtTestRestController {
	private final static Logger logger = LogManager.getLogger();

	@Autowired MailWorker mailWorker;
	@Autowired private WebContentRepository	 	webContentRepository;
	@Autowired private RemoteAddressRepository	 remoteAddressRepository;

	@PostMapping("get")
	public Object get(@RequestParam String url) throws ClientProtocolException, IOException{
		logger.error(url);

			HttpGet httpGet = new HttpGet(url.replaceAll(" ","%20"));
			try(	CloseableHttpClient httpClient = HttpClients.createDefault();
					CloseableHttpResponse response = httpClient.execute(httpGet);){

				return EntityUtils.toString(response.getEntity(), "UTF-8");
		}
	}

	@PostMapping("post")
	public Object post(@RequestParam String url, @RequestParam String data) throws ClientProtocolException, IOException{
		logger.error("\n\t{}\n\t{}", url, data);

			HttpPost httpGet = new HttpPost(url.replaceAll(" ","%20"));
			try(	CloseableHttpClient httpClient = HttpClients.createDefault();
					CloseableHttpResponse response = httpClient.execute(httpGet);){

				return response;
		}
	}

	@PostMapping("page_valiables")
    List<VariableContent> getPageVariables(@RequestParam String pageName) {
		logger.traceEntry(pageName);

    	return webContentRepository.findByPageName(pageName).stream().map(WebContent::getVariableContent).collect(Collectors.toList());
    }

	@PostMapping("email/send")
    ResponseMessage emailSend(@RequestBody WebEmail webEmail, HttpServletRequest request) {
		logger.traceEntry("{}", webEmail);

		Optional<String> filter = Optional.ofNullable(webEmail.getMessage()).map(String::trim).filter(m->!m.isEmpty()).filter(m->m.length()<=1000);
		if(!filter.isPresent())
			return new ResponseMessage("The maximum number of text characters is 1000.", BootstapClass.TXT_BG_WARNING);

		filter = Optional.ofNullable(webEmail.getEmail()).map(String::trim).filter(m->!m.isEmpty()).filter(m->m.length()<=320);
		if(!filter.isPresent())
			return new ResponseMessage("The maximum number of email characters is 320.", BootstapClass.TXT_BG_WARNING);

		final LocalDateTime now = LocalDateTime.now();
		final ResponseMessage responseMessage;
		final String remoteAddr = Optional.ofNullable(request.getRemoteAddr()).orElse(request.getRemoteHost());
		final Optional<RemoteAddress> byId = remoteAddressRepository.findById(remoteAddr);

		final RemoteAddress remoteAddress;
		if(byId.isPresent()) {

			remoteAddress = byId.get();

			// NOT_TRUSTED
			if(remoteAddress.getTrustStatus().equals(TrustStatus.NOT_TRUSTED)) 

				return new ResponseMessage(remoteAddress.getAddress() + " - Your IP address is blacklisted.", BootstapClass.TXT_BG_BLACK);
			else {

				// too much activity. Put in the black list.
				final int connectionCount = remoteAddress.getConnectionCount();
				if(connectionCount>10) {

					remoteAddress.setTrustStatus(TrustStatus.NOT_TRUSTED);

					responseMessage = new ResponseMessage(remoteAddress.getAddress() + " - Your IP address is blacklisted.", BootstapClass.TXT_BG_DANGER);

				}else {

					final LocalDateTime firstConnection = remoteAddress.getFirstConnection();
					final LocalDateTime lastConnection = remoteAddress.getLastConnection();
					final long betweenHours = ChronoUnit.HOURS.between(firstConnection, lastConnection);

					if(betweenHours>24) {

						// Reset Once a day.
						remoteAddress.setConnectionCount(1);
						remoteAddress.setFirstConnection(now);
					}else

						// Update connection count.
						remoteAddress.setConnectionCount(connectionCount + 1);

					final long betweenMin = ChronoUnit.MINUTES.between(lastConnection, now);

					if(betweenMin<15)
						responseMessage = new ResponseMessage("The next message can only be sent after 15 minutes.", BootstapClass.TXT_BG_WARNING);

					else
						responseMessage = sendEmail(webEmail);
				}
			}

		}else {

			// NEW ip address
			remoteAddress = new RemoteAddress(remoteAddr, now, now, 1, TrustStatus.UNKNOWN);
			responseMessage = sendEmail(webEmail);
		}

		remoteAddress.setLastConnection(now);
		remoteAddressRepository.save(remoteAddress);

		return responseMessage;
    }

	private ResponseMessage sendEmail(WebEmail webEmail) {

		final List<WebContent> byPageName = webContentRepository.findByPageName("email");
		final IrtEMailData irtEMailData = new IrtEMailData(byPageName);

		if(irtEMailData.isFilled()) {

			synchronized (IrtEMailData.class) {

				mailWorker.sendEmail(irtEMailData.getFrom(), "EMail from the web", webEmail.getHtml(), BodyType.HTML, irtEMailData.getTo());

				try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { logger.catching(Level.DEBUG, e); }

			}
			return new ResponseMessage("Thank you. We have received your email.", BootstapClass.TXT_BG_SUCCESS);

		}else
			return new ResponseMessage("We are having problems with this form.", BootstapClass.TXT_BG_WARNING);
	}

	@AllArgsConstructor @Getter
	public class ResponseMessage{
		private String message;
		private BootstapClass cssClass;
	}

	@AllArgsConstructor @Getter @ToString
	public enum BootstapClass{

		TXT_BG_DANGER("text-bg-danger"),
		TXT_BG_SUCCESS("text-bg-success"),
		TXT_BG_PRIMARY("text-bg-primary"),
		TXT_BG_BLACK("text-bg-black"),
		TXT_BG_WARNING("text-bg-warning");

		@JsonValue
		private String value;
	}
}
