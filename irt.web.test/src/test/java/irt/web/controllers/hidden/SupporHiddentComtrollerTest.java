package irt.web.controllers.hidden;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

class SupporHiddentComtrollerTest {
	private final Logger logger = LogManager.getLogger();

	@Test
	void oldGuiTest() {
		Map<String, String> model = new HashMap<>();
		File folder = new File("/var/irt/web/files/gui");

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

											model.put("version", next);

										} catch (IOException e) {
											logger.catching(e);
										}
									});

						} catch (IOException e) {
							logger.catching(e);
						}
					});
		logger.error(model);
		assertFalse(model.isEmpty());
	}

	@Test
	void newGuiTest() {
		Map<String, String> model = new HashMap<>();
		File folder = new File("/var/irt/web/files/gui4");

		if(folder.exists())

			Stream.of(folder.listFiles()).findAny()
			.ifPresent(
					f->{

						try(	final ZipFile jar = new ZipFile(f); ) {

							Collections.list(jar.entries()).stream().filter(j->j.getName().equals("BOOT-INF/classes/application.properties")).findAny()
							.ifPresent(
									irtGuiClass->{
										try(	final InputStream is = jar.getInputStream(irtGuiClass);
												final Scanner scanner = new Scanner(is);) {

											String next = null;

											while(scanner.hasNextLine()) {

												next = scanner.nextLine().trim();

												if(next.startsWith("info.app.version="))
													break;
											}

											model.put("version", next.replace("info.app.version=", ""));

										} catch (IOException e) {
											logger.catching(e);
										}
									});

						} catch (IOException e) {
							logger.catching(e);
						}
					});
		logger.error(model);
		assertFalse(model.isEmpty());
	}
}
