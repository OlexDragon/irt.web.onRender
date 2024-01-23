package irt.web.bean.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;

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
}
