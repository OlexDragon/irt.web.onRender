package irt.web.bean.email;

import java.util.List;
import java.util.Optional;

import irt.web.bean.Encoder;
import irt.web.bean.jpa.WebContent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class IrtEMailData {

	private String to;
	private String from;
	private String password;
	private String clientId;
	private String objectId;
	private String tenantId;
	private String secretId;
	private String clientSecret;	// id: 56a62c42-8e27-4ec4-ad02-e0bac1923743; Client Secret: p4-8Q~XNgXRbe-m0dcja9wRUrybeNmwlNR-a1aCt; Expires: 1/31/2026;
									// Client Secret: p4-8Q~XNgXRbe-m0dcja9wRUrybeNmwlNR-a1aCt;
									// Expires: 1/31/2026;
									// link to renew: https://entra.microsoft.com/#view/Microsoft_AAD_RegisteredApps/ApplicationMenuBlade/~/Credentials/appId/697654a8-3939-4eac-b85a-8f6336ec1cc6/isMSAApp~/false

	public IrtEMailData(List<WebContent> webContents) {

		webContents.forEach(
				c->{

					if(!c.getPageName().equals("email"))
						throw new IllegalArgumentException();

					switch (c.getNodeId()) {

					case "emailTo":
						to = c.getValue();
						break;

					case "emailFrom":
						from = c.getValue();
						break;

					case "emailPassword":
						password = Encoder.decode(c.getValue());
						break;

					case "clientId":
						clientId = c.getValue();
						break;

					case "objectId":
						objectId = c.getValue();
						break;

					case "tenantId":
						tenantId = c.getValue();
						break;

					case "secretId":
						secretId = c.getValue();
						break;

					case "clientSecret":
						clientSecret = c.getValue();
						break;
					}
				});
	}

	public boolean isFilled() {

		return Optional.ofNullable(to).filter(v->!v.isEmpty()).isPresent() &&
				Optional.ofNullable(from).filter(v->!v.isEmpty()).isPresent() &&
				Optional.ofNullable(password).filter(v->!v.isEmpty()).isPresent() &&
				Optional.ofNullable(clientId).filter(v->!v.isEmpty()).isPresent() &&
				Optional.ofNullable(objectId).filter(v->!v.isEmpty()).isPresent() &&
				Optional.ofNullable(tenantId).filter(v->!v.isEmpty()).isPresent() &&
				Optional.ofNullable(secretId).filter(v->!v.isEmpty()).isPresent() &&
				Optional.ofNullable(clientSecret).filter(v->!v.isEmpty()).isPresent();
	}
}
