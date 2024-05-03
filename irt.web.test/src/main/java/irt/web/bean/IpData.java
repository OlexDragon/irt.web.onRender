package irt.web.bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString @JsonIgnoreProperties(ignoreUnknown = true)
public class IpData {
/*{
	"ip":"0.0.0.0",
	"network":"0.0.0.0/24",
	"version":"IPv4",
	"city":"Montreal",
	"region":"Quebec",
	"region_code":"QC",
	"country":"CA",
	"country_name":"Canada",
	"country_code":"CA",
	"country_code_iso3":"CAN",
	"country_capital":"Ottawa",
	"country_tld":".ca",
	"continent_code":"NA",
	"in_eu":false,
	"postal":"H3C",
	"latitude":45.4978,
	"longitude":-73.5485,
	"timezone":"America/Toronto",
	"utc_offset":"-0400",
	"country_calling_code":"+1",
	"currency":"CAD",
	"currency_name":"Dollar",
	"languages":"en-CA,fr-CA,iu",
	"country_area":9984670,
	"country_population":37058856,
	"asn":"AS577",
	"org":"BACOM"}
*/
	private String ip;
	private String city;
	private String region;
	@JsonProperty("country_name")
	private String countryName;
	private String postal;
	private String timezone;

	public String toHtml() throws IOException {

		final InputStream resourceAsStream = getClass().getResourceAsStream("/static/webEmailIpData.html");

		try(	final InputStreamReader is = new InputStreamReader(resourceAsStream);
				final BufferedReader bufferedReader = new BufferedReader(is);){

			final String result = bufferedReader.lines().collect(Collectors.joining());

			return String.format(result, ip, city, region, countryName, postal, timezone);
		}
	}
}
