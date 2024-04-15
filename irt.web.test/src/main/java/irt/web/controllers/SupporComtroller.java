package irt.web.controllers;

import static irt.web.controllers.OnRenderController.getActiveMenuItems;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import irt.web.bean.ProductMenu;
import irt.web.bean.jpa.Faq;
import irt.web.bean.jpa.FaqRepository;
import irt.web.bean.jpa.WebContent;
import irt.web.bean.jpa.WebContent.ValueType;
import irt.web.bean.jpa.WebContentId;
import irt.web.bean.jpa.WebContentRepository;
import irt.web.bean.jpa.WebMenuRepository;

@Controller
@RequestMapping("support")
public class SupporComtroller {
	private final Logger logger = LogManager.getLogger();

	private static final String SIZE = "{size}";
	private static final String DATE = "{date}";
	private static final String VERSION = "{version}";
	private static final String NAME = "{name}";
	private static final String[] NAMES = {NAME, VERSION, DATE, SIZE};
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss");
	private static final WebContentId WEB_CONTENT_ID = new WebContentId("suport", "guiText",  ValueType.TEXT);

	@Value("${irt.web.root.path}")
	private String root;
	@Value("${irt.web.files.path}")
	private String filesPath;

	@Autowired private WebMenuRepository	menuRepository;
	@Autowired private WebContentRepository	 webContentRepository;
	@Autowired private FaqRepository	 	 faqRepository;

	@ModelAttribute("menus")
	public List<ProductMenu> menus() {
		return getActiveMenuItems(menuRepository);
	}

	@GetMapping
    String support(Model model){

		final Path path = Paths.get(root, filesPath, "gui");

		final File file = path.toFile();

		if (!file.exists()) {
			model.addAttribute(WEB_CONTENT_ID.getNodeId(), "Sorry, file not found.");

		}else
			webContentRepository.findById(WEB_CONTENT_ID).map(WebContent::getValue)
			.ifPresent(
				text->{

					final Optional<File> oFile = Stream.of(file.listFiles()).findAny();
					oFile.ifPresent(

							f->{
								Map<String, String> map = new HashMap<>();
								map.put(NAME, f.getName());

								try {

									final Path p = f.toPath();
									long size = Files.size(p);
									final Long kilobytes = size / 1024;
									map.put(SIZE, kilobytes + " kilobytes");

								} catch (IOException e) {
									logger.catching(e);
								}

								addVersionAndDate(oFile, map);

								String t = text;
								for(String n: NAMES)
									t = t.replace(n, Optional.ofNullable(map.get(n)).orElse("N/A"));

								model.addAttribute(WEB_CONTENT_ID.getNodeId(), t);
							});
				});

		final Iterable<Faq> all = faqRepository.findAll();
		logger.trace(all);
		model.addAttribute("allFAQs", all);

		return "support";
    }

	private void addVersionAndDate(Optional<File> oFile, Map<String, String> map) {

		oFile
		.ifPresent(
				f->{
					try(final ZipFile jar = new ZipFile(f);) {
						
						Collections.list(jar.entries()).stream().filter(j->j.getName().equals("irt/irt_gui/IrtGui.class")).findAny()
						.ifPresent(
								irtGuiClass->{

									LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(irtGuiClass.getTime()), ZoneId.of("Canada/Eastern")); 
									map.put(DATE, dt.format(DATE_FORMATTER));

									try(	final InputStream is = jar.getInputStream(irtGuiClass);
											final Scanner scanner = new Scanner(is);) {
								
										String next = null;

										while(scanner.hasNext()) {
											next = scanner.next().trim();
											if(next.startsWith("3.")) {
												map.put(VERSION, next);
												break;
											}
										}


									} catch (IOException e) {
										logger.catching(e);
									}
								});

					} catch (IOException e) {
						logger.catching(e);
					}
				});
	}
}
