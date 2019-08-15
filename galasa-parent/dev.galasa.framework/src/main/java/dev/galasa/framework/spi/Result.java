package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;

public class Result {

	private static final String IGNORED = "Ignored";
	private static final String PASSED  = "Passed";
	private static final String FAILED  = "Failed";

	private String name;
	private String reason;
	private String iconUri = "internalicon:unknown";
	
	private Throwable throwable;

	private boolean passed = false;
	private boolean failed = false;
	private boolean defects = false;
	private boolean ignored = false;
	private boolean environment = false;
	private boolean resources = false;
	private boolean fullStop = false;

	private Result() {}

	public static Result passed() {
		Result result  = new Result();
		result.name    = PASSED;
		result.passed  = true;
		result.iconUri = "internalicon:passed";

		return result;
	}

	public static Result failed(Throwable t) {
		Result result    = new Result();
		result.name      = FAILED;
		result.failed    = true;
		result.fullStop  = true;
		result.iconUri   = "internalicon:failed";
		result.reason    = t.getMessage();
		result.throwable = t;

		return result;
	}

	public static Result failed(String reason) {
		Result result   = new Result();
		result.name     = FAILED;
		result.failed   = true;
		result.fullStop = true;
		result.iconUri  = "internalicon:failed";
		result.reason   = reason;

		return result;
	}

	public static Result ignore(String reason) {
		Result result  = new Result();
		result.name    = IGNORED;
		result.ignored = true;
		result.iconUri = "internalicon:ignored";
		result.reason  = reason;

		return result;
	}

	public static Result custom(String managerResult) { //TODO create proper custom result from managers
		Result result   = new Result();
		result.name     = FAILED;
		result.failed   = true;
		result.fullStop = true;
		result.iconUri  = "internalicon:failed";
		result.reason   = managerResult;

		return result;
	}

	public @NotNull String getName() {
		return this.name;
	}

	public boolean isPassed() {
		return this.passed;
	}

	public boolean isFailed() {
		return this.failed;
	}
	public boolean isFullStop() {
		return this.fullStop;
	}
	
	public Throwable getThrowable() {
		return this.throwable;
	}


}
