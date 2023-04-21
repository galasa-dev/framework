/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.mocks;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

import org.junit.Test;

import com.google.gson.*;

import java.io.PrintWriter;


import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.framework.api.ras.internal.BaseServlet;
import dev.galasa.framework.api.ras.internal.mocks.*;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import javax.servlet.http.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class MockServletBaseEnvironment {
		
    MockFramework mockFramework;
    MockArchiveStore archiveStore;
    IServletUnderTest servlet;

    HttpServletRequest req;
    List<IResultArchiveStoreDirectoryService> directoryServices;
    List<IRunResult> mockInputRunResults;
    ByteArrayOutputStream outStream;
    PrintWriter writer;
        
    HttpServletResponse resp;

    public MockServletBaseEnvironment(List<IRunResult> mockInpResults, Map<String, String[]> parameterMap){ 
        this(mockInpResults, parameterMap, new MockResultArchiveStoreDirectoryService(mockInpResults));
    }

    public MockServletBaseEnvironment(List<IRunResult> mockInpResults, Map<String, String[]> parameterMap, MockResultArchiveStoreDirectoryService rasStore ){ 
        this.setMockInputs(mockInpResults);
        this.directoryServices = getDirectoryService(rasStore);
        this.setArchiveStore(new MockArchiveStore(this.directoryServices));
        this.mockFramework = new MockFramework(this.archiveStore);
        this.req = new MockHttpServletRequest(parameterMap);
        this.outStream = new ByteArrayOutputStream();
        this.writer = new PrintWriter(this.outStream);
        this.resp = new MockHttpServletResponse(this.writer);
        this.servlet = createServlet();
        this.servlet.setFramework(this.mockFramework);
    }


    public abstract IServletUnderTest createServlet();

    public HttpServletResponse getResponse (){
        return this.resp;
    }

    public HttpServletRequest getRequest (){
        return this.req;
    }

    public BaseServlet getBaseServlet(){
        return (BaseServlet)this.servlet;
    }

    public ByteArrayOutputStream getOutStream(){
        return this.outStream;
    }

    public void setMockInputs(List<IRunResult> mockInpResults){
        this.mockInputRunResults = mockInpResults;
    }

    public List<IResultArchiveStoreDirectoryService> getDirectoryService(MockResultArchiveStoreDirectoryService rasStore){
        List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
        directoryServices.add(rasStore);
        return directoryServices;
    }

    public void setArchiveStore(MockArchiveStore store){
        this.archiveStore = store;
    }

}