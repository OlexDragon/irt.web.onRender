package irt.web.bean.jpa;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import irt.web.bean.ConnectTo;

public interface IpConnectionRepository extends CrudRepository<IpConnection, Long> {

	List<IpConnection> findByIpIdAndConnectToAndDateGreaterThan(Long ipId, ConnectTo connectTo, LocalDateTime date);
}
