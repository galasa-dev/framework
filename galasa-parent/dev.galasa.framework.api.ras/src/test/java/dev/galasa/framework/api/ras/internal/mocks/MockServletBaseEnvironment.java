/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.mocks;

import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;

import java.io.PrintWriter;

import dev.galasa.framework.api.common.mocks.IServletUnderTest;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockServletOutputStream;
import dev.galasa.framework.api.ras.internal.RasServlet;
import dev.galasa.framework.mocks.MockFileSystem;

import java.util.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public abstract class MockServletBaseEnvironment {

    MockFramework mockFramework;
    MockFileSystem mockFileSystem;
    MockArchiveStore archiveStore;
    IServletUnderTest servlet;

    HttpServletRequest req;
    List<IResultArchiveStoreDirectoryService> directoryServices;
    List<IRunResult> mockInputRunResults;
    ServletOutputStream outStream;
    PrintWriter writer;

    HttpServletResponse resp;

    public MockServletBaseEnvironment(List<IRunResult> mockInpResults, Map<String, String[]> parameterMap) {
        this(mockInpResults, new MockHttpServletRequest(parameterMap), new MockResultArchiveStoreDirectoryService(mockInpResults));
    }

    public MockServletBaseEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest, MockFileSystem mockFileSystem) {
        this(mockInpResults, mockRequest, new MockResultArchiveStoreDirectoryService(mockInpResults));
        this.mockFileSystem = mockFileSystem;
        this.servlet.setFileSystem(this.mockFileSystem);
    }

    public MockServletBaseEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest, MockResultArchiveStoreDirectoryService rasStore ) {
        this.setMockInputs(mockInpResults);
        this.directoryServices = setDirectoryService(rasStore);
        this.setArchiveStore(new MockArchiveStore(this.directoryServices));
        this.mockFramework = new MockFramework(this.archiveStore);
        this.req = mockRequest;
        this.outStream = new MockServletOutputStream();
        this.writer = new PrintWriter(this.outStream);
        this.resp = new MockHttpServletResponse(this.writer, this.outStream);
        this.servlet = createServlet();
        this.servlet.setFramework(this.mockFramework);
    }


    public abstract IServletUnderTest createServlet();

    public HttpServletResponse getResponse() {
        return this.resp;
    }

    public HttpServletRequest getRequest() {
        return this.req;
    }

    public RasServlet getRasServlet() {
        return (RasServlet)this.servlet;
    }

    public void setMockInputs(List<IRunResult> mockInpResults) {
        this.mockInputRunResults = mockInpResults;
    }

    public List<IResultArchiveStoreDirectoryService> setDirectoryService(MockResultArchiveStoreDirectoryService rasStore) {
        List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
        directoryServices.add(rasStore);
        return directoryServices;
    }

    public List<IResultArchiveStoreDirectoryService> getDirectoryService() {
        return this.directoryServices;
    }

    public void setArchiveStore(MockArchiveStore store) {
        this.archiveStore = store;
    }

}