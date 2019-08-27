package dev.galasa.framework.api.testcatlog.internal;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * Basic Test Catalog store
 * 
 * @author Michael Baylis
 *
 */
@Component(
		service=Servlet.class,
		scope=ServiceScope.PROTOTYPE,
		property= {"osgi.http.whiteboard.servlet.pattern=/testcatalog"},
		configurationPid= {"dev.galasa"},
		configurationPolicy=ConfigurationPolicy.OPTIONAL,
		name="Galasa Test Catalogs"
		)
public class Testcatalogs extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Log logger = LogFactory.getLog(Testcatalogs.class);

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private Path catalogDirectory; // NOSONAR

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		try {
			checkDirectory();

			//*** locate the cached list and obtain the modified date
			boolean rebuildCache = false;
			FileTime cacheLastModified = FileTime.fromMillis(0);
			Path cachePath = catalogDirectory.resolve("cache.json");
			if (Files.exists(cachePath)) {
				cacheLastModified = Files.getLastModifiedTime(cachePath);
			} else {
				rebuildCache = true;
			}


			//*** Get a list of the streams and newest date
			CatalogConsumer consumer = new CatalogConsumer(cacheLastModified);
			try(Stream<Path> stream = Files.list(catalogDirectory)) {
				stream.forEach(consumer);
			}


			List<Path> catalogs = consumer.getCatalogs();

			JsonObject jsonCache = null;
			if (rebuildCache || consumer.rebuildCache()) {
				jsonCache = rebuildCacheFile(catalogs, cachePath);
			} else {
				jsonCache = gson.fromJson(Files.newBufferedReader(cachePath), JsonObject.class);
			}

			String jsonResponse = gson.toJson(jsonCache);

			resp.setContentType("application/json");
			resp.setContentLengthLong(jsonResponse.length());
			IOUtils.write(jsonResponse, resp.getOutputStream(), "utf-8");

		} catch(IOException e) {
			throw e;
		}

		resp.setStatus(200);
	}


	private JsonObject rebuildCacheFile(List<Path> catalogs, Path cachePath) throws JsonSyntaxException, JsonIOException, IOException {
		JsonObject jsonCache = new JsonObject();
		JsonObject  jsonCatalogs = new JsonObject();
		jsonCache.add("catalogs", jsonCatalogs);


		for(Path pathCatalog : catalogs) {
			JsonObject jsonCatalog = gson.fromJson(Files.newBufferedReader(pathCatalog), JsonObject.class);

			if (jsonCatalog != null) {
				JsonObject jsonCacheCatalog = new JsonObject();
				jsonCatalogs.add(pathCatalog.getFileName().toString(), jsonCacheCatalog);

				jsonCacheCatalog.add("name", jsonCatalog.getAsJsonObject("name"));
				jsonCacheCatalog.add("built", jsonCatalog.getAsJsonObject("build"));
			}
		}

		String data = gson.toJson(jsonCache);
		Files.write(cachePath, data.getBytes("utf-8"));

		return jsonCache;
	}


	private void checkDirectory() throws IOException {
		synchronized (Testcatalogs.class) {
			if (catalogDirectory == null) {
				catalogDirectory = Paths.get(System.getProperty("karaf.data")).resolve("galasa").resolve("testcatalogs");
			}
			if (!Files.exists(catalogDirectory)) {
				Files.createDirectories(catalogDirectory);
			}
		}
	}


	@Activate
	void activate(Map<String, Object> properties) {
		modified(properties);
	}

	@Modified
	void modified(Map<String, Object> properties) {
		Object oDirectoryProperty = properties.get("framework.testcatalog.directory");
		if (oDirectoryProperty != null && oDirectoryProperty instanceof String) {
			String directoryProperty = (String) oDirectoryProperty;
			try {
				catalogDirectory = Paths.get(new URL(directoryProperty).toURI());
				logger.info("Catalog directorty set to " + catalogDirectory.toUri().toString());
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			catalogDirectory = null;
		}
	}

	@Deactivate
	void deactivate() {
		this.catalogDirectory = null;
	}

	private static final class CatalogConsumer implements Consumer<Path> {

		private ArrayList<Path> catalogs = new ArrayList<>();
		private FileTime cacheLastModified;
		private boolean rebuildCache = false;

		private CatalogConsumer(FileTime cacheLastModified) {
			this.cacheLastModified = cacheLastModified;
		}

		@Override
		public void accept(Path t) {
			if (Files.isRegularFile(t) && !t.getFileName().toString().equals("cache.json")) {
				catalogs.add(t);

				try {
					FileTime catalogtime = Files.getLastModifiedTime(t);

					if (catalogtime.compareTo(cacheLastModified) >= 0) {
						rebuildCache = true;
					}
				} catch (IOException e) {
					rebuildCache = true;
					e.printStackTrace();
				}
			}
		}

		private boolean rebuildCache() {
			return this.rebuildCache;
		}

		public List<Path> getCatalogs() {
			return this.catalogs;
		}

	}

}
