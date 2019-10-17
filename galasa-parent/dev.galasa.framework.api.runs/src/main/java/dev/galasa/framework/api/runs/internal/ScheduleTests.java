package dev.galasa.framework.api.runs.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.Gson;

import dev.galasa.framework.api.runs.bind.RunStatus;
import dev.galasa.framework.api.runs.bind.ScheduleRequest;
import dev.galasa.framework.api.runs.bind.ScheduleStatus;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRun;


/**
 * Schedule Tests API
 * 
 * Allows for a set of Tests to be scheduled and their state to be inquired
 * 
 */
@Component(
		service=Servlet.class,
		scope=ServiceScope.PROTOTYPE,
		property= {"osgi.http.whiteboard.servlet.pattern=/run/*"},
		configurationPid= {"dev.galasa"},
		configurationPolicy=ConfigurationPolicy.REQUIRE,
		name="Galasa Run Test"
		)
public class ScheduleTests extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Reference
	public IFramework framework;   // NOSONAR
	
	private static Logger logger;

	private final Properties configurationProperties = new Properties();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String UUID = getUUID(req);
		List<IRun> runs = null;
		try {
			runs = framework.getFrameworkRuns().getAllGroupedRuns(UUID);
		}catch(FrameworkException fe) {
			logger.severe("Unable to obtain framework runs for UUID: " + UUID);
			resp.setStatus(500);
			return;
		}
		ScheduleStatus status = new ScheduleStatus();
		boolean complete = true;
		for(IRun run : runs) {
			if(!"FINISHED".equalsIgnoreCase(run.getStatus())) {
				complete = false;
			}
				
			status.getRuns().add(run.getSerializedRun());
		}
				
		if(complete) {
			status.setScheduleStatus(RunStatus.FINISHED_RUN);
		}else {
			status.setScheduleStatus(RunStatus.TESTING);
		}
		
		resp.setStatus(200);
		resp.setHeader("Content-Type", "Application/json");
		
		try {
			resp.getWriter().write(new Gson().toJson(status));
		}catch(IOException ioe) {
			logger.severe("Unable to respond to requester" + System.lineSeparator() + ioe.getStackTrace());
			resp.setStatus(500);
		}
	}
	
	@Override 
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		boolean submissionFailures = false;
		String UUID = getUUID(req);
		ScheduleRequest request;
		try {
			request = (ScheduleRequest)new Gson().fromJson(new InputStreamReader(req.getInputStream()), ScheduleRequest.class);
		}catch(Exception e) {
			logger.warning("Error understanding / receiving run test request");
			resp.setStatus(500);
			return;
		}
		for(String className : request.getClassNames()) {
			String bundle = className.split("/")[0];
			String testClass = className.split("/")[1];
			try {
				framework.getFrameworkRuns().submitRun(null, request.getRequestorType().toString(), bundle, testClass, UUID, request.getMavenRepository(), request.getObr(), request.getTestStream(), false, request.isTrace(), null);
			}catch(FrameworkException fe) {
				logger.warning("Failure when submitting run: " + className + System.lineSeparator() + fe.getStackTrace());
				submissionFailures = true;
				continue;
			}
			
		}
		if(!submissionFailures) {
			resp.setStatus(200);
		}else {
			resp.setStatus(500);
		}
	}

	private String getUUID(HttpServletRequest req) {
		return req.getPathInfo().substring(1);
	}
	
	@Activate
	void activate(Map<String, Object> properties) {
		modified(properties);
		this.logger = Logger.getLogger(this.getClass().toString());
	}

	@Modified
	void modified(Map<String, Object> properties) {
		// TODO set the JWT signing key etc
	}

	@Deactivate
	void deactivate() {
		//TODO Clear the properties to prevent JWT generation
	}

}
