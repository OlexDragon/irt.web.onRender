package irt.web.bean.jpa;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table
@IdClass(ProductFilterId.class)
@Getter @Setter @ToString(exclude = {"filter", "product", "productFilters"})
public class ProductFilter implements Serializable{
	private static final long serialVersionUID = 1122645113246112147L;

	@Id private Long	 productId;
	@Id private Long	 filterId;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filterId", referencedColumnName = "id", insertable = false, updatable = false)
    private Filter filter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="productId", referencedColumnName = "id", insertable = false, updatable = false)
    private Product product;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name="productId", referencedColumnName = "productId", insertable = false, updatable = false)
	private List<ProductFilter> productFilters;
}
