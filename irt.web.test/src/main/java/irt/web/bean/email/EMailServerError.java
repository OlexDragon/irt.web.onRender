package irt.web.bean.email;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor @Getter @Setter @ToString
public class EMailServerError {

	private String error;
	private String error_description;
	private List<Integer> error_codes;
	private String trace_id;
	private String correlation_id;
	private String error_uri;
	private String timestamp;
}
