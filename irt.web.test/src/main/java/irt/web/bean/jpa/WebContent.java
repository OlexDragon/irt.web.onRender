package irt.web.bean.jpa;

import jakarta.persistence.Id;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Data
@IdClass(WebContentId.class)
@NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString
public class WebContent{

	@Id private String	pageName;
	@Id private String	nodeId;

	private String 	value;

	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = true, columnDefinition = "default '0'")
	private ValueType valueType;

	@Setter
	private VariableContent variableContent;

	public WebContentId getId() {
		return new WebContentId(pageName, nodeId, valueType);
	}

	public enum ValueType{
		VALUE,
		TEXT,
		CLASS,
		HREF;
	}
}
