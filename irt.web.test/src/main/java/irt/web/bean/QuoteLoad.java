package irt.web.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class QuoteLoad {

	private QuoteCustomer customer;
	private QuoteRedundancy redundancy;
	private QuoteType type;
}
