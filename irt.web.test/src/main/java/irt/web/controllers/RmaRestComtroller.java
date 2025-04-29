package irt.web.controllers;

import static irt.web.controllers.OnRenderRestController.getMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import irt.web.bean.ConnectTo;
import irt.web.bean.RmaByIDsRequest;
import irt.web.bean.RmaCountByStatus;
import irt.web.bean.RmaData;
import irt.web.bean.RmaRequest;
import irt.web.bean.TrustStatus;
import irt.web.bean.jpa.Email;
import irt.web.bean.jpa.EmailRepository;
import irt.web.bean.jpa.IpAddress;
import irt.web.bean.jpa.IpConnection;
import irt.web.bean.jpa.Rma;
import irt.web.bean.jpa.Rma.Status;
import irt.web.bean.jpa.RmaRepository;
import irt.web.bean.jpa.SerialNumber;
import irt.web.bean.jpa.SerialNumberRepository;
import irt.web.bean.jpa.UserName;
import irt.web.bean.jpa.UserNameRepository;
import irt.web.bean.jpa.WebContent;
import irt.web.bean.jpa.WebContent.ValueType;
import irt.web.bean.jpa.WebContentId;
import irt.web.bean.jpa.WebContentRepository;
import irt.web.controllers.OnRenderRestController.BootstapClass;
import irt.web.controllers.OnRenderRestController.ResponseMessage;
import irt.web.service.IpService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.NonNull;

@RestController
@RequestMapping("rest/rma")
public class RmaRestComtroller {
	private final Logger logger = LogManager.getLogger();
	private final static int MAX_SIZE = 100;

	@Autowired private IpService ipService;
	@Autowired private EntityManager			 entityManager;
	@Autowired private SerialNumberRepository	 serialNumberRepository;
	@Autowired private WebContentRepository		 webContentRepository;
	@Autowired private UserNameRepository		 nameRepository;
	@Autowired private EmailRepository			 emailRepository;
	@Autowired private RmaRepository			 rmaRepository;

	@GetMapping("ready-to-add")
    boolean exists(@RequestParam String sn){

		if(sn.replaceAll("\\D", "").length()!=7)
			return false;

		// if starts with number
		if(Character.isDigit(sn.charAt(0))) {
			final Pageable page = PageRequest.of(0, 1);
			return !serialNumberRepository.findBySerialNumberEndingWithIgnoreCase(sn, page).parallelStream().findAny()
					.map(SerialNumber::getId)
					.map(id->rmaRepository.existsBySerialNumberIdAndStatusNotIn(id, Status.SHIPPED, Status.CLOSED))
					.orElse(false);
		}

		return !rmaRepository.existsBySerialNumberSerialNumberIgnoreCaseAndStatusNotIn(sn, Status.SHIPPED, Status.CLOSED) &&
				serialNumberRepository.existsBySerialNumberIgnoreCase(sn);
	}
	@GetMapping("count")
    List<RmaCountByStatus> count(){
		return rmaRepository.countByStatus();
	}

	@GetMapping("by-id")
    RmaData byId(@RequestParam Long rmaId){
		return rmaRepository.findById(rmaId).map(RmaData::new).orElse(null);
	}

	@PostMapping(path = "by-ids", produces = MediaType.APPLICATION_JSON_VALUE )
    List<RmaData> byIds(@RequestBody RmaByIDsRequest rmaByIDs){

		final PageRequest page =  getPageRequest(rmaByIDs.getDirection(), rmaByIDs.getName(), rmaByIDs.getSize());
		final List<Long> ids = rmaByIDs.getRmaIds();

		return Optional.ofNullable(rmaByIDs.getStatus()).filter(s->s.length>0)

				.map(st->rmaRepository.findByIdInAndStatusIn(ids, page, st))
				.map(List::spliterator)
				.map(spliterator->StreamSupport.stream(spliterator, false))
				.map(stream->stream.map(RmaData::new))
				.map(stream->stream.collect(Collectors.toList()))

				.orElseGet(
						()->StreamSupport
						.stream(rmaRepository.findByIdIn(ids, page).spliterator(), false)
						.map(RmaData::new)
						.collect(Collectors.toList()));
	}

	@GetMapping("by-rma") @Transactional
    List<RmaData> byRma(
    		@RequestParam String like,
    		@RequestParam(required = false) Status[] status,
    		@RequestParam(required = false) Boolean statusNot,
    		@RequestParam(required = false) Direction direction,
    		@RequestParam(required = false) String name,
    		@RequestParam(required = false) Integer size){

		final PageRequest page =  getPageRequest(direction, name, size);

		return Optional.ofNullable(status).filter(s->s.length>0)

				.map(
						st->Optional.ofNullable(statusNot).filter(sn->sn).map(_->rmaRepository.findByRmaNumberContainingIgnoreCaseAndStatusNotIn(like, page, st))
									.orElseGet(()->rmaRepository.findByRmaNumberContainingIgnoreCaseAndStatusIn(like, page, st)))

				.orElseGet(()->rmaRepository.findByRmaNumberContainingIgnoreCase(like, page))
				.stream().map(RmaData::new).collect(Collectors.toList());
	}

	@GetMapping("by-serial") @Transactional
    List<RmaData> bySerial(
    		@RequestParam String like,
    		@RequestParam(required = false) Status[] status,
    		@RequestParam(required = false) Boolean statusNot,
    		@RequestParam(required = false) Direction direction,
    		@RequestParam(required = false) String name,
    		@RequestParam(required = false) Integer size){

		final PageRequest page =  getPageRequest(direction, name, size);

		return Optional.ofNullable(status)

			.map(
					st->
					Optional.ofNullable(statusNot).filter(sn->sn)
					.map(_->rmaRepository.findBySerialNumberSerialNumberContainingIgnoreCaseAndStatusNotIn(like, page, st))
					.orElseGet(()->rmaRepository.findBySerialNumberSerialNumberContainingIgnoreCaseAndStatusIn(like, page, st)))

			.orElseGet(()->rmaRepository.findBySerialNumberSerialNumberContainingIgnoreCase(like, page))
			.stream().map(RmaData::new).collect(Collectors.toList());
	}

	protected PageRequest getPageRequest(Direction direction, String name, Integer size) {

		return Optional.ofNullable(direction).filter(_->name!=null && !name.trim().isEmpty())
				.map(d-> Sort.by(d, name)).map(sort->PageRequest.of(0, Optional.ofNullable(size).orElse(MAX_SIZE), sort))
				.orElseGet(()->PageRequest.of(0, Optional.ofNullable(size).orElse(MAX_SIZE)));
	}

	@GetMapping("by-description") @Transactional
    List<RmaData> byDescription(
    		@RequestParam String like,
    		@RequestParam(required = false) Status[] status,
    		@RequestParam(required = false) Boolean statusNot,
    		@RequestParam(required = false) Direction direction,
    		@RequestParam(required = false) String name,
    		@RequestParam(required = false) Integer size){

		final PageRequest page =  getPageRequest(direction, name, size);

		return Optional.ofNullable(status)

				.map(
						st->
						Optional.ofNullable(statusNot).filter(sn->sn).map(_->rmaRepository.findBySerialNumberPartNumberDescriptionContainingIgnoreCaseAndStatusNotIn(like, page, st))
						.orElseGet(()->rmaRepository.findBySerialNumberPartNumberDescriptionContainingIgnoreCaseAndStatusIn(like, page, st)))

				.orElseGet(()->rmaRepository.findBySerialNumberPartNumberDescriptionContainingIgnoreCase(like, page)).stream().map(RmaData::new).collect(Collectors.toList());
	}

	@GetMapping("by-cause") @Transactional
    List<RmaData> byCause(
    		@RequestParam String like,
    		@RequestParam(required = false) Status[] status,
    		@RequestParam(required = false) Boolean statusNot,
    		@RequestParam(required = false) Direction direction,
    		@RequestParam(required = false) String name,
    		@RequestParam(required = false) Integer size){

		final PageRequest page =  getPageRequest(direction, name, size);

		return Optional.ofNullable(status)

				.map(
						st->
						Optional.ofNullable(statusNot).filter(sn->sn).map(_->rmaRepository.findByMalfunctionContainingIgnoreCaseAndStatusNotIn(like, page, st))
						.orElseGet(()->rmaRepository.findByMalfunctionContainingIgnoreCaseAndStatusIn(like, page, st)))

				.orElseGet(()->rmaRepository.findByMalfunctionContainingIgnoreCase(like, page))
				.stream().map(RmaData::new).collect(Collectors.toList());
	}

	@GetMapping("by-status") @Transactional
    List<RmaData> byStatus(
    		@RequestParam Status status,
    		@RequestParam(required = false) Boolean statusNot,
    		@RequestParam(required = false) Direction direction,
    		@RequestParam(required = false) String name,
    		@RequestParam(required = false) Integer size){

		final PageRequest page =  getPageRequest(direction, name, size);

		return Optional.ofNullable(statusNot).filter(sn->sn).map(_->rmaRepository.findByStatusNot(status, page))
		.orElseGet(()->rmaRepository.findByStatus(status, page))
		.stream().map(RmaData::new).collect(Collectors.toList());
	}

	@GetMapping("ids-by-status")
    List<Long> idsByStatus(@RequestParam Status[] status){

		final CriteriaBuilder criteriaBuilder	 = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Long> criteriaQuery	 = criteriaBuilder.createQuery(Long.class);
		final Root<Rma> root					 = criteriaQuery.from(Rma.class);
		final Object[] statusIds = Arrays.stream(status).map(s->s.ordinal()).toArray();
		criteriaQuery.where(root.get("status").in(statusIds));
		final CriteriaQuery<Long> select = criteriaQuery.select(root.get("id"));

		return entityManager.createQuery(select).getResultList();
	}

	@PostMapping("create") @Transactional
    ResponseMessage createRma(HttpServletRequest request, @RequestBody RmaRequest rmaRequest){
		final String remoteAddr = Optional.ofNullable(request.getHeader( "X-Forwarded-For" )).orElseGet(()->request.getRemoteAddr());
		logger.traceEntry("IP: {}; rmaRequest: {}", remoteAddr, rmaRequest);

		// Check IP address
		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(remoteAddr);

		// No clientIP or NOT_TRUSTED
		if(!oIpAddress.filter(ra->ra.getTrustStatus()!=TrustStatus.NOT_TRUSTED).isPresent()) {
			final String message = "You are on the blacklist. ( #" + remoteAddr + " )";
			logger.warn(message + "\n\t" + rmaRequest);
			return getMessage(message, BootstapClass.TXT_BG_DANGER);
		}

		// 2 min. between messages
		final int minTime = 2;
		final IpAddress ipAddress = oIpAddress.get();
		final Long ipAddressId = ipAddress.getId();
		final List<IpConnection> connectionsIn5min = ipService.getConnections(ipAddressId, ConnectTo.WEB_EMAIL, LocalDateTime.now(ZoneId.of("Canada/Eastern")).minusMinutes(minTime));
		if(!connectionsIn5min.isEmpty()) {
			final String message = "The next RMA number can be generated in " + minTime + " minutes.";
			logger.warn(message);
			return getMessage(message, BootstapClass.TXT_BG_WARNING);
		}

		// Validate RmaRequest
		if(!rmaRequest.isValid()) {
			logger.warn("The RMA Request is not valid. {}", rmaRequest);
			return getMessage("The RMA Request is not valid.", BootstapClass.TXT_BG_DANGER);
		}

		// Serial Number
		final String sn = rmaRequest.getSn().toUpperCase();
		Optional<SerialNumber> oSerialNumber = serialNumberRepository.findBySerialNumber(sn);
		if(!oSerialNumber.isPresent()) {
			if(sn.length()==7 && sn.replaceAll("\\D", "").length()==7) {	// Only Numbers
				final Pageable page = PageRequest.of(0, 1);
				final List<SerialNumber> serialNumbers = serialNumberRepository.findBySerialNumberEndingWithIgnoreCase(sn, page);
				if(serialNumbers.isEmpty()) {
					final String message = "We did not manufacture the unit with this serial number: " + sn;
					logger.warn(message);
					return getMessage(message, BootstapClass.TXT_BG_DANGER);
				}else
					oSerialNumber = Optional.of(serialNumbers.get(0));
			}else {
				final String message = "We did not manufacture the unit with this serial number: " + sn;
				logger.warn(message);
				return getMessage(message, BootstapClass.TXT_BG_DANGER);
			}
		}

		// Check if exists
		final List<Rma> rmas = rmaRepository.findBySerialNumberSerialNumberIgnoreCaseAndStatusNot(sn, Status.SHIPPED);
		if(!rmas.isEmpty()) {
			final Rma rma = rmas.get(0);
			final String message = "The serial number " + sn + " already exists - " + rma.getRmaNumber() + statusToText(rma);
			logger.warn(message);
			return getMessage(message, BootstapClass.TXT_BG_WARNING);
		}

		// Create RMA
		final UserName userName = nameRepository.findByName(rmaRequest.getName()).orElseGet(()->nameRepository.save(new UserName(rmaRequest.getName())));
		final Email email = emailRepository.findByEmail(rmaRequest.getEmail()).orElseGet(()->emailRepository.save(new Email(rmaRequest.getEmail())));

		final Long connectionId = ipService.createConnection(ipAddressId, ConnectTo.RMA).getId();

		String rmaNumber = createNewRmaNumber();
		final Rma rma = new Rma(rmaNumber, Status.CREATED, rmaRequest.getCause(), oSerialNumber.get().getId(), userName.getId(), email.getId(), connectionId, connectionId);


		try {

			final Rma savedRma = rmaRepository.save(rma);

			final String shippingAddress = webContentRepository.findById(new WebContentId("irt", "address", ValueType.TEXT)).map(WebContent::getValue).map(sa->"<>" + sa).orElse("");
			final String json = new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(savedRma) + shippingAddress;
			logger.debug(json);

			return getMessage(json, BootstapClass.TXT_BG_SUCCESS);

		} catch (Exception e) {
			logger.catching(e);
			StringBuilder sb = new StringBuilder();
			return getMessage(getErrorMessage(sb, e), BootstapClass.TXT_BG_DANGER);
		}
	}

	@PostMapping("change/status")
    boolean changeStatus(HttpServletRequest request, @RequestParam Long rmaId, @RequestParam Rma.Status status){
		final String remoteAddr = Optional.ofNullable(request.getHeader( "X-Forwarded-For" )).orElseGet(()->request.getRemoteAddr());
		logger.traceEntry("clientIP: {}; rmaId: {}; status: {}", remoteAddr, rmaId, status);

		// Check IP address
		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(remoteAddr);

		// No clientIP or NOT_TRUSTED
		if(!oIpAddress.filter(ra->ra.getTrustStatus()!=TrustStatus.NOT_TRUSTED).isPresent()) {
			logger.warn("Not Trasted IP: {}", remoteAddr);
			return false;
		}

		return rmaRepository.findById(rmaId)
				.filter(rma->rma.getStatus()!=status)
				.map(
						rma->{
							final IpConnection connection = ipService.createConnection(oIpAddress.get().getId(), ConnectTo.RMA);
							rma.setStatus(status);
							rma.setStatusCangeDateId(connection.getId());
							rmaRepository.save(rma);
							return true;
						})
				.orElse(false);
	}

	private String getErrorMessage(StringBuilder sb, Throwable throwable) {

		sb.append(throwable.getLocalizedMessage()).append('\n');

		Arrays.stream(throwable.getStackTrace())
		.map(StackTraceElement::toString)
		.map(st->st.split("irt.", 2))
		.filter(st->st.length>1)
		.map(st->st[1].substring(st[1].indexOf('(')))
		.map(st->st.replaceAll(".java", ""))
		.forEach(st->sb.append(st).append('\n'));

		Optional.ofNullable(throwable.getCause()).ifPresent(st->getErrorMessage(sb, st));

		return sb.toString();
	}

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd, MMM yyyy");
	private String statusToText(@NonNull Rma rma) {

		String text;
		final String statusCange = rma.getCreationDate().getDate().format(formatter);
		switch(rma.getStatus()) {

		case CREATED:
			text = ".\n We have not received your RMA unit yet. RMA number created " + statusCange;
			break;

		default:
		case IN_WORK:
			text = ".\nRMA Status: The RMA unit is under investigation. RMA status change: " + statusCange;
			break;

		case READY:
			text = ".\n The RMA unit is ready and preparing to ship. RMA status change: " + statusCange;
			break;

		case SHIPPED:
			text = ".\n The RMA unit has been shipped. RMA status change: " + statusCange;
		}
		return text;
	}

	private final DateTimeFormatter rmaFormatter = DateTimeFormatter.ofPattern("'RMA'yyMM");
	private String createNewRmaNumber() {

		final LocalDate currentdate = LocalDate.now(ZoneId.of("Canada/Eastern"));
		final String format = currentdate.format(rmaFormatter);
		final int count = rmaRepository.findByRmaNumberStartsWithIgnoreCase(format).parallelStream().map(Rma::getRmaNumber).map(rmaNumber->rmaNumber.substring(7).split("[- ]")[0]).mapToInt(Integer::parseInt).max().orElse(0);
		final String sequence = String.format("%03d", count+1);

		return format + sequence;
	}
}
