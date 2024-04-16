package irt.web.bean.jpa;

import java.io.Serializable;

import jakarta.persistence.Id;

import irt.web.bean.jpa.WebContent.ValueType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString @EqualsAndHashCode
public class WebContentId implements Serializable{
	private static final long serialVersionUID = 1546518371942829868L;

	@Id  @Column(updatable = false, insertable = false) private String	pageName;
	@Id  @Column(updatable = false, insertable = false) private String	nodeId;

	@Id
	@Enumerated(EnumType.ORDINAL)
	@Column(updatable = false, insertable = false)
	private ValueType valueType;
}
