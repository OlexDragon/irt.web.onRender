package irt.web.bean.jpa;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Id;

import irt.web.bean.TrustStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table
@NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString
public class IpAddress implements Serializable{
	private static final long serialVersionUID = 6315117908794414784L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NonNull
	@Column(nullable = false, unique = true)
	private String 	address;
	@NonNull
	private LocalDateTime 	firstConnection;
	@NonNull
	@Enumerated(EnumType.ORDINAL)
	private TrustStatus trustStatus;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "ipId", referencedColumnName = "id", insertable = false, updatable = false)
	private List<IpConnection> connections;
}
