package io.ejat.framework.spi;

import java.time.Instant;

public interface IRun {
	
	String getName();

	Instant getHeartbeat();

}
