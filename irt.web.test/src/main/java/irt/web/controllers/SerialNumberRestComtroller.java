package irt.web.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import irt.web.bean.TrustStatus;
import irt.web.bean.jpa.IpAddress;
import irt.web.bean.jpa.PartNumber;
import irt.web.bean.jpa.PartNumberRepository;
import irt.web.bean.jpa.SerialNumber;
import irt.web.bean.jpa.SerialNumberRepository;
import irt.web.service.IpService;
import jakarta.servlet.http.HttpServletRequest; 

@RestController
@RequestMapping("rest/serial-number")
public class SerialNumberRestComtroller {
	private final Logger logger = LogManager.getLogger();

	@Autowired private IpService ipService;
	@Autowired private SerialNumberRepository	 serialNumberRepository;
	@Autowired private PartNumberRepository		 partNumberRepository;

	@GetMapping("exists")
    boolean exists(@RequestParam String sn){
		return serialNumberRepository.existsBySerialNumberIgnoreCase(sn);
	}

	@GetMapping("ends-with")
    Optional<SerialNumber> endsWith(@RequestParam String sn){
		Pageable page = PageRequest.of(0, 1);
		return serialNumberRepository.findBySerialNumberEndingWithIgnoreCase(sn, page).parallelStream().findAny();
	}

	private static ScheduledFuture<?> schedule;
	private final static Map<String, Integer> ipCount = new HashMap<>();
	private final static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	@PostMapping("save")
    boolean save(HttpServletRequest request, @RequestParam String sn, @RequestParam String pn, @RequestParam String descr){
		final String remoteAddr = Optional.ofNullable(request.getHeader( "X-Forwarded-For" )).orElseGet(()->request.getRemoteAddr());

		// Check IP address
		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(remoteAddr);

		// No clientIP or NOT_TRUSTED
		if(!oIpAddress.filter(ra->ra.getTrustStatus()==TrustStatus.IRT).isPresent()) {
			Optional.ofNullable(schedule).filter(sch->!sch.isCancelled() && !sch.isDone()).map(sch->sch.cancel(true));
			Runnable r = ()->{
				ipCount.entrySet()
				.forEach(
						e->{
							logger.warn("{} - Unauthorized access {} times. SN: {}; PN: {}; descr: {}", e.getKey(), e.getValue(), sn, pn, descr);
						});
				ipCount.clear();
			};
			Integer count = Optional.ofNullable(ipCount.get(remoteAddr)).orElse(0);
			ipCount.put(remoteAddr, ++count);
			schedule = executor.schedule(r, 1, TimeUnit.SECONDS);
			return false;
		}

		if(serialNumberRepository.existsBySerialNumberIgnoreCase(sn))
			return false;

		final Optional<PartNumber> oPartNumber = partNumberRepository.findByPartNumberIgnoreCase(pn);
		final PartNumber partNumber;
		if(oPartNumber.isPresent())
			partNumber = oPartNumber.get();
		else
			partNumber = partNumberRepository.save(new PartNumber(pn.toUpperCase(), descr));

		final String upperCase = sn.toUpperCase();
		final SerialNumber serialNumber = new SerialNumber(upperCase, partNumber.getId());
		serialNumberRepository.save(serialNumber);

		return true;
	}
}
