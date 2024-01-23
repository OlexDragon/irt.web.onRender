package irt.web.bean.jpa;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import irt.web.bean.jpa.WebContent.ValueType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString
public class VariableContent{

	@Column(insertable = false, updatable = false)
	private String	nodeId;

	@Column(insertable = false, updatable = false)
	private String 	value;

	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = true, insertable = false, updatable = false)
	private ValueType valueType;
}
