package irt.web.bean.jpa;

import irt.web.bean.jpa.WebContent.ValueType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
