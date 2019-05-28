package io.ejat.framework;

import java.util.ArrayList;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import io.ejat.framework.spi.IResultArchiveStore;
import io.ejat.framework.spi.ResultArchiveStoreException;

public class TestRunLogCapture  implements Appender {
	
	private final Framework framework;
	
	private IResultArchiveStore ras;
	
	private final ArrayList<String> startupCache = new ArrayList<>();
	private Layout layout = new PatternLayout("%d{HH:mm:ss} %p [%t] %c - %m%n");
	private Level minimumLevel = Level.ALL;

	
	private boolean shutdown = false;

	public TestRunLogCapture(Framework framework) {
		this.framework = framework;
		
		Logger rootLogger = Logger.getRootLogger();
		
		Appender stdout = rootLogger.getAppender("stdout");
		if (stdout != null) {
			this.layout = stdout.getLayout();
		}
		
		rootLogger.addAppender(this);
	}

	public void shutdown() {
		this.shutdown = true;
	}

	@Override
	public void doAppend(LoggingEvent event) {
		if (this.shutdown) {
			return;
		}
		
		if (!event.getLevel().isGreaterOrEqual(minimumLevel)) {
			return;
		}
		
		String message = layout.format(event);
		
		if (ras == null) {
			if (framework.isInitialised()) {
				this.ras = framework.getResultArchiveStore();
			} else {
				startupCache.add(message);
				return;
			}
		}

		if (!startupCache.isEmpty()) {
			try {
				this.ras.writeLog(startupCache);
				this.startupCache.clear();
			} catch (ResultArchiveStoreException e) { 
				e.printStackTrace(); //*** Do not use logger,  will cause a loop //NOSONAR
				startupCache.add(message);
				return;
			}
		}
		
		try {
			this.ras.writeLog(message);
		} catch (ResultArchiveStoreException e) { 
			e.printStackTrace(); //*** Do not use logger,  will cause a loop //NOSONAR
			startupCache.add(message);
		}
	}

	@Override
	public void addFilter(Filter newFilter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearFilters() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setErrorHandler(ErrorHandler errorHandler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ErrorHandler getErrorHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLayout(Layout layout) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Layout getLayout() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean requiresLayout() {
		// TODO Auto-generated method stub
		return false;
	}

}
