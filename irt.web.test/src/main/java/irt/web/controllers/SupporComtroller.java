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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import irt.web.bean.ProductMenu;
import irt.web.bean.jpa.Faq;
import irt.web.bean.jpa.FaqRepository;
import irt.web.bean.jpa.WebContent;
import irt.web.bean.jpa.WebContent.ValueType;
import irt.web.service.DocumentsService;
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
	private static final DateTimeFormatter DATE_FORMATTER_FR = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm:ss", Locale.FRENCH);
	private static final WebContentId WEB_CONTENT_ID = new WebContentId("suport", "guiText",  ValueType.TEXT);

	@Value("${irt.web.root.path}")
	private String root;
	@Value("${irt.web.files.path}")
	private String filesPath;

	@Autowired private WebMenuRepository	menuRepository;
//	@Autowired private WebContentRepository	webContentRepository;
	@Autowired private FaqRepository	 	faqRepository;
	@Autowired private DocumentsService		docService;

	@Autowired private MessageSource messageSource;

	@ModelAttribute("menus")
	public List<ProductMenu> menus() {
		return getActiveMenuItems(menuRepository);
	}

	@GetMapping
    String support(@CookieValue(required = false) String localeInfo, Model model){

		final Path path = Paths.get(root, filesPath, "gui4");

		final File file = path.toFile();

		// Set Language
		Optional<String> oLang = Optional.ofNullable(localeInfo).filter(s->s.equals("fr") || s.equals("en"));
		oLang.ifPresent(s->model.addAttribute("lang", s));
		final Locale locale = oLang.map(Locale::of).orElse(Locale.ENGLISH);

		if (!file.exists()) 
			model.addAttribute(WEB_CONTENT_ID.getNodeId(), messageSource.getMessage("page.support.fileNotFound", null, locale));

		else
			guiData(file, messageSource.getMessage("page.support.gui", null, locale), model, locale);
//			webContentRepository.findById(WEB_CONTENT_ID).map(WebContent::getValue).ifPresent(text->guiData(file, text, model));

		final Iterable<Faq> all = faqRepository.findAll();
		logger.trace(all);
		model.addAttribute("allFAQs", all);

		// Documentation
		docService.addDocuments(model);

		return "support";
    }

	public void guiData(final File file, String text, Model model, Locale locale) {
		final Optional<File> oFile = Stream.of(file.listFiles()).findAny();
		oFile.ifPresent(

				f->{
					Map<String, String> map = new HashMap<>();
					map.put(NAME, f.getName());

					try {

						final Path p = f.toPath();
						long size = Files.size(p);
						final Long kilobytes = size / 1024;
						map.put(SIZE, kilobytes + " " + messageSource.getMessage("kilobytes", null, locale));

					} catch (IOException e) {
						logger.catching(e);
					}

					addVersionAndDate(oFile, map, locale);

					String t = text;
					for(String n: NAMES)
						t = t.replace(n, Optional.ofNullable(map.get(n)).orElse("N/A"));

					model.addAttribute(WEB_CONTENT_ID.getNodeId(), t);
				});
	}

	private void addVersionAndDate(Optional<File> oFile, Map<String, String> map, Locale locale) {

		oFile
		.ifPresent(
				f->{
					try(final ZipFile jar = new ZipFile(f);) {
						
						Collections.list(jar.entries()).stream().filter(j->j.getName().equals("BOOT-INF/classes/application.properties")).findAny()
						.ifPresent(
								irtGuiClass->{

									LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(irtGuiClass.getTime()), ZoneId.systemDefault());
									DateTimeFormatter dFormatter = locale == Locale.FRENCH ? DATE_FORMATTER_FR : DATE_FORMATTER;
									map.put(DATE, dt.format(dFormatter));

									try(	final InputStream is = jar.getInputStream(irtGuiClass);
											final Scanner scanner = new Scanner(is);) {
								
										String next = null;

										while(scanner.hasNextLine()) {
											next = scanner.nextLine().trim();
											if(next.startsWith("info.app.version=")) {
												map.put(VERSION, next.replace("info.app.version=", ""));
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
