package dev.galasa;

import javax.validation.constraints.NotNull;

public class JsonError {

	private final String error;
	
	public JsonError(@NotNull String error) {
		
		this.error = error;
		
	}
	
}
