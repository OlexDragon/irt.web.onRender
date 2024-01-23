package irt.web.bean.jpa;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table
@IdClass(WebMenuFilterId.class)
@Getter @Setter @ToString(exclude = {"filter", "menu"})
public class WebMenuFilter implements Serializable{
	private static final long serialVersionUID = 1122645113246112147L;

	@Id private Long	 menuId;
	@Id private Long	 filterId;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filterId", referencedColumnName = "id", insertable = false, updatable = false)
    private Filter filter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="menuId", referencedColumnName = "id", insertable = false, updatable = false)
    private WebMenu menu;
}
