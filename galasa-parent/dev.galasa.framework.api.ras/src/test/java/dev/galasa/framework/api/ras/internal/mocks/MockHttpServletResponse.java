/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.mocks;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MockHttpServletResponse implements HttpServletResponse {

    private PrintWriter writer ;
    private int status;
    private String contentType ;
    private Map<String,String> headers ;

    public MockHttpServletResponse( PrintWriter writer) {
        this.writer = writer;
        this.headers = new HashMap<String,String>();
    }

    @Override
    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCharacterEncoding'");
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    
    public ServletOutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOutputStream'");
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return this.writer ;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCharacterEncoding'");
    }

    @Override
    public void setContentLength(int len) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setContentLength'");
    }

    @Override
    public void setContentLengthLong(long len) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setContentLengthLong'");
    }

    @Override
    public void setContentType(String type) {
        this.contentType = type ;
    }

    @Override
    public void setBufferSize(int size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setBufferSize'");
    }

    @Override
    public int getBufferSize() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBufferSize'");
    }

    @Override
    public void flushBuffer() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'flushBuffer'");
    }

    @Override
    public void resetBuffer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resetBuffer'");
    }

    @Override
    public boolean isCommitted() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isCommitted'");
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reset'");
    }

    @Override
    public void setLocale(Locale loc) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setLocale'");
    }

    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLocale'");
    }

    @Override
    public void addCookie(Cookie cookie) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addCookie'");
    }

    @Override
    public boolean containsHeader(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'containsHeader'");
    }

    @Override
    public String encodeURL(String url) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encodeURL'");
    }

    @Override
    public String encodeRedirectURL(String url) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encodeRedirectURL'");
    }

    @Override
    public String encodeUrl(String url) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encodeUrl'");
    }

    @Override
    public String encodeRedirectUrl(String url) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encodeRedirectUrl'");
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendError'");
    }

    @Override
    public void sendError(int sc) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendError'");
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendRedirect'");
    }

    @Override
    public void setDateHeader(String name, long date) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDateHeader'");
    }

    @Override
    public void addDateHeader(String name, long date) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addDateHeader'");
    }

    @Override
    public void setHeader(String name, String value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setHeader'");
    }

    @Override
    public void addHeader(String name, String value) {
        this.headers.put(name,value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setIntHeader'");
    }

    @Override
    public void addIntHeader(String name, int value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addIntHeader'");
    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
    }

    @Override
    public void setStatus(int sc, String sm) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setStatus'");
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public String getHeader(String name) {
       return this.headers.get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHeaders'");
    }

    @Override
    public Collection<String> getHeaderNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHeaderNames'");
    }
    
}