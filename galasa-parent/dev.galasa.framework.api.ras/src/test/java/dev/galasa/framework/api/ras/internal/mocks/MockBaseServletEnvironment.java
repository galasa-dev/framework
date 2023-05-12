package dev.galasa.framework.api.ras.internal.mocks;

import java.util.List;

import dev.galasa.framework.api.ras.internal.BaseServlet;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.spi.IRunResult;

public class MockBaseServletEnvironment extends MockServletBaseEnvironment {

	public MockBaseServletEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest){ 
    	super(mockInpResults, mockRequest, new MockResultArchiveStoreDirectoryService(mockInpResults));
	}

	public MockBaseServletEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest, MockFileSystem mockFileSystem ){ 
		super(mockInpResults, mockRequest, mockFileSystem);
	}

	public MockBaseServletEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest, MockResultArchiveStoreDirectoryService rasStore ){ 
		super(mockInpResults, mockRequest, rasStore);
	}

	public BaseServlet getServlet() {
		return super.getBaseServlet();
	}

	@Override
	public IServletUnderTest createServlet() {
    	return new MockBaseServlet();
	}
}
