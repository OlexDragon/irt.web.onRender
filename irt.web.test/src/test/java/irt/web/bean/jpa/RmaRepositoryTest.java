package irt.web.bean.jpa;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RmaRepositoryTest {
	private final Logger logger = LogManager.getLogger();

	@Autowired SerialNumberRepository snRepository;
	@Autowired RmaRepository repository;

	@Test @Transactional
	void test() {
		final Optional<SerialNumber> oSerialNumber = snRepository.findBySerialNumber("IRT-1929017");
		logger.error(oSerialNumber);
		final List<Rma> iRma = repository.findByRmaNumberStartsWithIgnoreCase("RMA2310007");
		logger.error(iRma);
		final List<Rma> rmas = repository.findBySerialNumberId(390L);
		logger.error(rmas);
		assertTrue(repository.existsBySerialNumberIdAndStatusNotIn(390L, Rma.Status.CLOSED));
	}

}
