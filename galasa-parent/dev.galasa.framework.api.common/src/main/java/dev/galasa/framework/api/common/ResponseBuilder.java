/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.api.common;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;

public class ResponseBuilder {

    public HttpServletResponse sendResponse(HttpServletResponse resp, String contentType, String content, int status){
		//Set headers for HTTP Response
		resp.setStatus(status);
		resp.setContentType(contentType);
		resp.addHeader("Access-Control-Allow-Origin", "*");
		try (PrintWriter out = resp.getWriter()) {
			out.print(content);
		} catch (Exception e) {
			// TODO : log the exception, saying we are going to ignore it.
		}
		return resp;
	}
}
