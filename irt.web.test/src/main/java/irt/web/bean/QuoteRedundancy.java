package irt.web.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString  @JsonInclude(Include.NON_NULL)
public class QuoteRedundancy {

	private Boolean need;
	private RedundancyType type;
	private Integer qty;

	public enum RedundancyType {

		ONE_TO_ONE,
		ONE_TO_TWO,
		CONVERTER;
	}
}
