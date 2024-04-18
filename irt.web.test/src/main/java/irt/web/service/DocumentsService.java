package irt.web.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import jakarta.annotation.PostConstruct;

@Service
public class DocumentsService {
	private final Logger logger = LogManager.getLogger();

	@Value("${irt.web.root.path}")	private String root;
	@Value("${irt.web.files.path}")	private String filesPath;

	private Path docsPath;

	@PostConstruct
	public void postConstruct() {
		docsPath = Paths.get(root, filesPath, "docs");
		docsPath.toFile().mkdirs();
	}

	public void addDocuments(Model model) {
		final Map<String, List<String>> docs = new HashMap<>();
		try {
			Files.walk(docsPath)
			.filter(p->p.getNameCount() > docsPath.getNameCount())
			.forEach(p->{

				final String parent;
				final String name;
				if(p.toFile().isFile()) {

					parent = p.getParent().getFileName().toString();
					name = p.getFileName().toString();

				}else {

					parent = p.getFileName().toString();
					name = "";
				}

				final List<String> nameList = Optional.ofNullable(docs.get(parent))

						.orElseGet(
								()->{
									final ArrayList<String> list = new ArrayList<>();
									docs.put(parent, list);
									return list;
								});

				if(!name.isEmpty())
					nameList.add(name);
			});

			model.addAttribute("docs", docs);

		} catch (IOException e) {
			logger.catching(e);
		}
	}
}
