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

	public IrtEMailData(List<WebContent> webContents) {

		webContents.forEach(
				c->{

					if(!c.getPageName().equals("email"))
						throw new IllegalArgumentException();

					switch (c.getNodeId()) {

					case "emailTo":
						to = Encoder.decode(c.getValue());
						break;

					case "emailFrom":
						from = Encoder.decode(c.getValue());
						break;

					case "emailPassword":
						password = Encoder.decode(c.getValue());
						break;

					case "clientId":
						clientId = Encoder.decode(c.getValue());
						break;

					case "objectId":
						objectId = Encoder.decode(c.getValue());
						break;

					case "tenantId":
						tenantId = Encoder.decode(c.getValue());
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
				Optional.ofNullable(tenantId).filter(v->!v.isEmpty()).isPresent();
	}
}
