package irt.web.bean.email;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import irt.web.bean.IpData;
import irt.web.bean.jpa.WebContent;
import irt.web.bean.jpa.WebContentRepository;
import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CurlMailWorkerTest {

	@Autowired private MailWorker mailWorker;
	@Autowired private WebContentRepository	 	webContentRepository;

	private IrtEMailData irtEMailData;

	@PostConstruct
	void postConstruct() {

		final List<WebContent> byPageName = webContentRepository.findByPageName("email");
		irtEMailData = new IrtEMailData(byPageName);
		irtEMailData.setTo("oleksandrp@irttechnologies.com");
	}

	@Test
	void test() throws IOException {

		final WebEmail webEmail = new WebEmail();
		webEmail.setFirstName("First name");
		webEmail.setLastName("Last  name");
		webEmail.setPhone("Phone");
		webEmail.setEmail("Email");
		webEmail.setCompany("Company");
		webEmail.setIndustry("Industry");
		webEmail.setMessage("Test Main Worker");


		final IpData ipData = new IpData();
		webEmail.setIpData(Optional.of(ipData));
		ipData.setIp("IP");
		ipData.setCity("City");
		ipData.setRegion("Region");
		ipData.setCountryName("Country");
		ipData.setPostal("Postal");
		ipData.setTimezone("Time Zone");

		mailWorker.sendEmail(webEmail, irtEMailData);
	}

}
