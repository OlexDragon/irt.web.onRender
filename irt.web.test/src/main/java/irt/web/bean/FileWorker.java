package irt.web.bean;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

public class FileWorker {
	protected final Logger logger = LogManager.getLogger(getClass());

	protected void saveFile(Path folder, MultipartFile mpFile) throws IllegalStateException, IOException {
		logger.traceEntry("folder: {};", folder);

		String originalFilename = mpFile.getOriginalFilename();

		folder.toFile().mkdirs();	//create a directories

		Path path = folder.resolve(originalFilename);

		mpFile.transferTo(path);
	}

}
