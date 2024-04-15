package irt.web.bean;

import irt.web.bean.jpa.WebContent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor @Getter @Setter @ToString
public class AboutPage {

	private String header;
	private String title;
	private String text;

	private final Address address = new Address();

	public AboutPage(Iterable<WebContent> aboutContent) {
		aboutContent.forEach(
				wc->{
					switch(wc.getNodeId()) {

					case "aHeader":
						header = wc.getValue();
						break;

					case "aTitle":
						title = wc.getValue();
						break;

					case "aText":
						text = wc.getValue();
						break;

					case "aCompanyName":
						address.setCompanyName(wc.getValue());
						break;

					case "aAddress":
						address.setAddress(wc.getValue());
						break;

					case "aEmail":
						address.setEmail(wc.getValue());
						break;

					case "aPhon":
						address.setPhone(wc.getValue());
						break;

					case "aFax":
						address.setFax(wc.getValue());
						break;
					}
				});
	}
}
