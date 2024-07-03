package irt.web.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonValue;

import irt.web.bean.IpData;
import irt.web.bean.ConnectTo;
import irt.web.bean.TrustStatus;
import irt.web.bean.email.IrtEMailData;
import irt.web.bean.email.MailWorker;
import irt.web.bean.email.WebEmail;
import irt.web.bean.jpa.IpAddress;
import irt.web.bean.jpa.IpConnection;
import irt.web.bean.jpa.VariableContent;
import irt.web.bean.jpa.WebContent;
import irt.web.bean.jpa.WebContentRepository;
import irt.web.service.IpService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@RestController
@RequestMapping("rest")
public class OnRenderRestController {
	private final static Logger logger = LogManager.getLogger();

	@Autowired MailWorker mailWorker;
	@Autowired private WebContentRepository	 	webContentRepository;
	@Autowired private IpService ipService;

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
    ResponseMessage emailSend(@CookieValue(required = false) Optional<IpData> ipData, @RequestBody WebEmail webEmail, HttpServletRequest request) {
		final String remoteAddr = request.getRemoteAddr();
		logger.info("Client IP: {}; remoteAddr: {}", ipData, remoteAddr);

		final LocalDateTime now = LocalDateTime.now(ZoneId.of("Canada/Eastern"));

		// Check IP address
		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(remoteAddr);

		// No clientIP or NOT_TRUSTED
		if(!oIpAddress.filter(ra->ra.getTrustStatus()!=TrustStatus.NOT_TRUSTED).isPresent()) {
			final String message = "You are on the blacklist.";
			logger.warn(message + ".\n" + webEmail);
			return getMessage(message, BootstapClass.TXT_BG_DANGER);
		}

		// 5 min. between messages
		final int minTime = 5;
		final IpAddress ipAddress = oIpAddress.get();
		final Long id = ipAddress.getId();
		final List<IpConnection> connectionsIn5min = ipService.getConnections(id, ConnectTo.WEB_EMAIL, now.minusMinutes(minTime));
		if(!connectionsIn5min.isEmpty()) {
			final String message = "The next message can only be sent after " + minTime + " minutes";
			logger.info(message);
			return getMessage(message, BootstapClass.TXT_BG_WARNING);
		}

		// Add to the Blacklist
		ipService.createConnection(id, ConnectTo.WEB_EMAIL);
		final List<IpConnection> connections = ipService.getConnections(ipAddress.getId(), ConnectTo.WEB_EMAIL, now.minusMinutes(minTime * 20));
		if(ipAddress.getTrustStatus()==TrustStatus.UNKNOWN && connections.size()>10) {
			ipAddress.setTrustStatus(TrustStatus.NOT_TRUSTED);
			ipService.save(ipAddress);
			final String message = "You are on the blacklist.";
			logger.warn(message);
			return getMessage(message, BootstapClass.TXT_BG_DANGER);
		}

		// Maximum message length
		final String userMessage = webEmail.getMessage();
		Optional<String> filter = Optional.ofNullable(userMessage).map(String::trim).filter(m->!m.isEmpty()).filter(m->m.length()<=1000);
		if(!filter.isPresent()) {
			final String message = "The maximum number of text characters is 1000.";
			logger.info("\n\t{},\n{}", message, userMessage);
			return getMessage(message, BootstapClass.TXT_BG_WARNING);
		}

		// Maximum email length
		final String email = webEmail.getEmail();
		filter = Optional.ofNullable(email).map(String::trim).filter(m->!m.isEmpty()).filter(m->m.length()<=320);
		if(!filter.isPresent()) {
			final String message = "The maximum number of email characters is 320.";
			logger.info("\n\t{},\n{}", message, email);
			return getMessage(message, BootstapClass.TXT_BG_WARNING);
		}

		try {

			webEmail.setIpData(ipData);
			return sendEmail(webEmail);

		} catch (IOException e) {
			logger.catching(e);
			return getMessage(e.getLocalizedMessage(), BootstapClass.TXT_BG_DANGER);
		}
    }

	public static ResponseMessage getMessage(final String message, BootstapClass bootstapClass) {
		final ResponseMessage responseMessage = new ResponseMessage(message, bootstapClass);
		logger.debug(responseMessage);
		return responseMessage;
	}

	private static LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Canada/Eastern")); 

	private ResponseMessage sendEmail(WebEmail webEmail) throws IOException {

		final List<WebContent> byPageName = webContentRepository.findByPageName("email");
		final IrtEMailData irtEMailData = new IrtEMailData(byPageName);

		if(irtEMailData.isFilled()) {

			synchronized (IrtEMailData.class) {

				while(ChronoUnit.SECONDS.between(dateTime, LocalDateTime.now(ZoneId.of("Canada/Eastern")))<5) {
					try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { logger.catching(Level.DEBUG, e); }	
				}
				dateTime = LocalDateTime.now(ZoneId.of("Canada/Eastern"));

				final ResponseMessage status = mailWorker.sendEmail(webEmail, irtEMailData);

				return status;
			}

		}else
			return new ResponseMessage("We are having problems with this form.", BootstapClass.TXT_BG_WARNING);
	}

	@AllArgsConstructor @Getter @ToString
	public static class ResponseMessage{
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

	@ExceptionHandler(Exception.class)
	  public String handleError(Exception ex) {
		logger.catching(ex);
	    return ex.getLocalizedMessage();
	  }
}
