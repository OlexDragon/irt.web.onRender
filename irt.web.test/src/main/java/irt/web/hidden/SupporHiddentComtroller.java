package irt.web.hidden;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import irt.web.bean.TrustStatus;
import irt.web.bean.jpa.IpAddress;
import irt.web.service.IpService;

@Controller
@RequestMapping("hidden/support")
public class SupporHiddentComtroller {
	private final Logger logger = LogManager.getLogger();

	@Value("${irt.web.root.path}")
	private String root;
	@Value("${irt.web.files.path}")
	private String filesPath;

	@Autowired private IpService ipService;

	private Path filesFolder;

	@PostConstruct
	public void postConstruct() {
		filesFolder = Paths.get(root, filesPath);
	}

	@GetMapping
    String support(@CookieValue(required = false) String clientIP, Model model){

		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(clientIP);

		if(!oIpAddress.filter(addr->addr.getTrustStatus()==TrustStatus.IRT).isPresent()) {
			model.addAttribute("errorCode", clientIP);
			logger.info("{} redirected to error page", oIpAddress);
			return "error";
		}

		final File folder = filesFolder.resolve("gui").toFile();

		if(folder.exists())

			Stream.of(folder.listFiles()).findAny()
			.ifPresent(
					f->{

						try(	final ZipFile jar = new ZipFile(f); ) {

							Collections.list(jar.entries()).stream().filter(j->j.getName().equals("irt/irt_gui/IrtGui.class")).findAny()
							.ifPresent(
									irtGuiClass->{
										try(	final InputStream is = jar.getInputStream(irtGuiClass);
												final Scanner scanner = new Scanner(is);) {

											String next = null;

											while(scanner.hasNext()) {

												next = scanner.next().trim();

												if(next.startsWith("3."))
													break;
											}

											model.addAttribute("version", next);

										} catch (IOException e) {
											logger.catching(e);
										}
									});

						} catch (IOException e) {
							logger.catching(e);
						}
					});

		return "hidden/support";
    }
}
