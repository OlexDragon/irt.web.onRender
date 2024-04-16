package irt.web.bean.jpa;

import java.io.Serializable;

import jakarta.persistence.Id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter @Setter @ToString @EqualsAndHashCode
public class ProductFilterId implements Serializable{
	private static final long serialVersionUID = 1122645113246112147L;

	@Id
	@Column(updatable = false, insertable = false)
	private Long	 productId;
	@Id
	@Column(updatable = false, insertable = false)
	private Long	 filterId;
}
