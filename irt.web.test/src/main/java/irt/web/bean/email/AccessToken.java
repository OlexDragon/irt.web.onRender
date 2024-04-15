package irt.web.bean.email;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class AccessToken {

	private String token_type;
	private Integer expires_in;
	private Integer ext_expires_in;
	private Integer expires_on;
	private Integer not_before;
	private String resource;
	private String access_token;
}
