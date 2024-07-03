package irt.web.controllers;

import java.util.Optional;

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

	@PostMapping("save")
    boolean save(HttpServletRequest request, @RequestParam String sn, @RequestParam String pn, @RequestParam String descr){
		final String remoteAddr = request.getRemoteAddr();

		// Check IP address
		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(remoteAddr);

		// No clientIP or NOT_TRUSTED
		if(!oIpAddress.filter(ra->ra.getTrustStatus()==TrustStatus.IRT).isPresent()) {
			logger.warn("Unauthorized access. ( {} )", oIpAddress.map(IpAddress::getId).orElse(-1L));
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
