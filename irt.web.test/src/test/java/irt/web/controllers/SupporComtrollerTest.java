package irt.web.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import irt.web.bean.jpa.WebContent.ValueType;
import irt.web.bean.jpa.WebContentId;

class SupporComtrollerTest {
	private final Logger logger = LogManager.getLogger();
	private static final String SIZE = "{size}";
	private static final String DATE = "{date}";
	private static final String VERSION = "{version}";
	private static final String NAME = "{name}";
	private static final String[] NAMES = {NAME, VERSION, DATE, SIZE};
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss");
	private static final WebContentId WEB_CONTENT_ID = new WebContentId("support", "guiText",  ValueType.TEXT);

	@Test
	void test() {

		File file = new File("/var/irt/web/files/gui");
		String text = "To download GUI click <a href=\"/files/gui\" class=\"link-warning\"  target=\"_blank\">here</a>.\r\n"
				+ "The IRT graphical interface is universal and works with all our products.\r\n"
				+ "\r\n"
				+ "  File Name:          {name}\r\n"
				+ "  Version:              {version}\r\n"
				+ "  Release date::     {date}\r\n"
				+ "  JAR file size:       {size}\r\n"
				+ "\r\n"
				+ "To run the JAR file, <a href=\"https://www.java.com/en/download/\" target=\"_blank\" class=\"link-warning\">install Java</a> on your computer if it is not already installed.";
		Map<String, String> model = new HashMap<>();
		oldGuiData(file, text, model);
		logger.error("\n{}", model);
	}

	@Test
	void newGuiTest() {

		File file = new File("/var/irt/web/files/gui4");
		String text = "To download GUI click <a href=\"/files/gui\" class=\"link-warning\"  target=\"_blank\">here</a>.\r\n"
				+ "The IRT graphical interface is universal and works with all our products.\r\n"
				+ "\r\n"
				+ "  File Name:          {name}\r\n"
				+ "  Version:              {version}\r\n"
				+ "  Release date::     {date}\r\n"
				+ "  JAR file size:       {size}\r\n"
				+ "\r\n"
				+ "To run the JAR file, <a href=\"https://www.java.com/en/download/\" target=\"_blank\" class=\"link-warning\">install Java</a> on your computer if it is not already installed.";
		Map<String, String> model = new HashMap<>();
		newGuiData(file, text, model);
		logger.error("\n{}", model);
	}

	public void oldGuiData(final File file, String text, Map<String, String> model) {
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

					model.put(WEB_CONTENT_ID.getNodeId(), t);
					logger.error(map);
				});
	}

	private void addVersionAndDate(Optional<File> oFile, Map<String, String> map) {

		oFile
		.ifPresent(
				f->{
					try(final ZipFile jar = new ZipFile(f);) {
						
						Collections.list(jar.entries()).stream().filter(j->j.getName().equals("irt/irt_gui/IrtGui.class")).findAny()
						.ifPresent(
								irtGuiClass->{

									LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(irtGuiClass.getTime()), ZoneId.systemDefault()); 
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
	public void newGuiData(final File file, String text, Map<String, String> model) {
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

					addNewGuiVersionAndDate(oFile, map);

					String t = text;
					for(String n: NAMES)
						t = t.replace(n, Optional.ofNullable(map.get(n)).orElse("N/A"));

					model.put(WEB_CONTENT_ID.getNodeId(), t);
					logger.error(map);
				});
	}


	private void addNewGuiVersionAndDate(Optional<File> oFile, Map<String, String> map) {

		oFile
		.ifPresent(
				f->{
					try(final ZipFile jar = new ZipFile(f);) {
						
						Collections.list(jar.entries()).stream().filter(j->j.getName().equals("BOOT-INF/classes/application.properties")).findAny()
						.ifPresent(
								irtGuiClass->{

									LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(irtGuiClass.getTime()), ZoneId.systemDefault()); 
									map.put(DATE, dt.format(DATE_FORMATTER));

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
