package irt.web.bean.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;

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
		return new WebContentId(pageName, nodeId);
	}

	public enum ValueType{
		VALUE,
		TEXT,
		CLASS;
	}
}
