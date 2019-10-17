package dev.galasa.framework;

import java.time.Instant;

public class SerializedRun {
	private String name;
	private Instant heartbeat;
	private String type;
	private String group;
	private String test;
	private String bundleName;
	private String testName;
	private String status;
	private String result;
	private Instant queued;
	private Instant finished;
	private Instant waitUntil;
	private String requestor;
	private String stream;
	private String repo;
	private String obr;
	private Boolean local;
	private Boolean trace;
	
	public SerializedRun(String name, Instant heartbeat, String type, String group, String test, String bundleName,
			String testName, String status, String result,Instant queue, Instant finished, Instant waitUntil, String requestor,
			String stream, String repo, String obr, Boolean local, Boolean trace) {
		this.name = name;
		this.heartbeat = heartbeat;
		this.type= type;
		this.group = group;
		this.test = test;
		this.bundleName = bundleName;
		this.testName = testName;
		this.status = status;
		this.result = result;
		this.queued = queued;
		this.finished = finished;
		this.waitUntil = waitUntil;
		this.requestor = requestor;
		this.stream = stream;
		this.repo = repo;
		this.obr = obr;
		this.local = local;
		this.trace = trace;
	}
	public String getName() {
		return name;
	}
	public Instant getHeartbeat() {
		return heartbeat;
	}
	public String getType() {
		return type;
	}
	public String getGroup() {
		return group;
	}
	public String getTest() {
		return test;
	}
	public String getBundleName() {
		return bundleName;
	}
	public String getTestName() {
		return testName;
	}
	public String getStatus() {
		return status;
	}
	public Instant getQueued() {
		return queued;
	}
	public Instant getFinished() {
		return finished;
	}
	public Instant getWaitUntil() {
		return waitUntil;
	}
	public String getRequestor() {
		return requestor;
	}
	public String getStream() {
		return stream;
	}
	public String getRepo() {
		return repo;
	}
	public String getObr() {
		return obr;
	}
	public Boolean getLocal() {
		return local;
	}
	public Boolean getTrace() {
		return trace;
	}
	public String getResult() {
		return result;
	}

}
