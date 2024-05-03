package irt.web.bean.email;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import irt.web.bean.IpData;

class WebEmailTest {
	private final static Logger logger = LogManager.getLogger();

	@Test
	void test() throws IOException {

		final WebEmail webEmail = new WebEmail();
		assertFalse(webEmail.toHtml().contains("Client IP Data"));

		webEmail.setFirstName("First name");
		webEmail.setLastName("Last  name");
		webEmail.setPhone("Phone");
		webEmail.setEmail("Email");
		webEmail.setCompany("Company");
		webEmail.setIndustry("Industry");
		webEmail.setMessage(" IRT Technologies, an acronym for Intelligent RF Telecom technologies, designs, develops and manufactures advanced satellite RF systems and products for real time voice, data and multimedia delivery anywhere in the world.\r\n"
				+ "\r\n"
				+ "   IRT products are revolutionary innovative, super compact, efficient and reliable, serving both commercial and government sectors.\r\n"
				+ "\r\n"
				+ "   IRT satellite solutions shapes the next-generation communication equipment with its breakthrough technology, vanguard research and development, state of the art engineering design, and product innovation.  ");


		final IpData ipData = new IpData();
		webEmail.setIpData(Optional.of(ipData));
		ipData.setIp("IP");
		ipData.setCity("City");
		ipData.setRegion("Region");
		ipData.setCountryName("Country");
		ipData.setPostal("Postal");
		ipData.setTimezone("Time Zone");

		logger.error(webEmail.toHtml());
		assertTrue(webEmail.toHtml().contains("Client IP Data"));
	}

}
