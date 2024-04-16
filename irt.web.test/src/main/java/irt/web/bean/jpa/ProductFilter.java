package irt.web.bean.jpa;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
}
