package irt.web.bean.jpa;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import irt.web.bean.jpa.Rma.Status;


public interface RmaRepository extends CrudRepository<Rma, Long> {

	List<Rma> findByStatus		(Status status, Pageable page);
	List<Rma> findByStatusNot	(Status status, Pageable page);

	List<Rma> findByIdIn			(List<Long> rmaIds, Pageable page);
	List<Rma> findByIdInAndStatusIn	(List<Long> rmaIds, Pageable page, Status... status);

	List<Rma> findByRmaNumberContainingIgnoreCase				(String like, Pageable page);
	List<Rma> findByRmaNumberContainingIgnoreCaseAndStatusIn	(String like, Pageable page, Status... status);
	List<Rma> findByRmaNumberContainingIgnoreCaseAndStatusNotIn	(String like, Pageable page, Status... status);
	List<Rma> findByRmaNumberStartsWithIgnoreCase				(String string);

	List<Rma> findBySerialNumberId(Long id);
	List<Rma> findBySerialNumberSerialNumberContainingIgnoreCase				(String like, Pageable page);
	List<Rma> findBySerialNumberSerialNumberContainingIgnoreCaseAndStatusIn		(String like, Pageable page, Status... status);
	List<Rma> findBySerialNumberSerialNumberContainingIgnoreCaseAndStatusNotIn	(String like, Pageable page, Status... status);
	List<Rma> findBySerialNumberSerialNumberIgnoreCaseAndStatus							(String serialNumber, Status status);
	List<Rma> findBySerialNumberSerialNumberIgnoreCaseAndStatusNot						(String serialNumber, Status status);

	List<Rma> findBySerialNumberPartNumberDescriptionContainingIgnoreCase				(String like, Pageable page);
	List<Rma> findBySerialNumberPartNumberDescriptionContainingIgnoreCaseAndStatusIn	(String like, Pageable page, Status... status);
	List<Rma> findBySerialNumberPartNumberDescriptionContainingIgnoreCaseAndStatusNotIn	(String like, Pageable page, Status... status);

	List<Rma> findByMalfunctionContainingIgnoreCase					(String like, Pageable page);
	List<Rma> findByMalfunctionContainingIgnoreCaseAndStatusIn		(String like, Pageable page, Status... status);
	List<Rma> findByMalfunctionContainingIgnoreCaseAndStatusNotIn	(String like, PageRequest page, Status... st);

	boolean existsBySerialNumberIdAndStatusNotIn(Long snId, Status... status);
	boolean existsBySerialNumberSerialNumberIgnoreCaseAndStatusIn		(String sn, Status... status);
	boolean existsBySerialNumberSerialNumberIgnoreCaseAndStatusNotIn	(String sn, Status... status);
}
