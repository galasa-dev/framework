/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResponseBuilder {

	protected Log logger  =  LogFactory.getLog(ResponseBuilder.class);

    public HttpServletResponse buildResponse(
		HttpServletResponse resp,
		String contentType,
		String content,
		int status
	) {
		//Set headers for HTTP Response
		resp.setStatus(status);
		resp.setContentType(contentType);
		resp.addHeader("Access-Control-Allow-Origin", "*");

		try(PrintWriter out = resp.getWriter()) {
			out.print(content);
			out.flush();
		} catch (Exception ex) {
			// TODO: Should we actually ignore these exceptions ? How bad is this ?
			logger.warn("Failed to build response object. Ignoring.", ex);
		}
		return resp;
	}

	public HttpServletResponse buildResponseHeaders(
		HttpServletResponse resp,
		String contentType,
		int status
	) {
		//Set headers for HTTP Response
		resp.setStatus(status);
		resp.setContentType(contentType);
		resp.addHeader("Access-Control-Allow-Origin", "*");

		return resp;
	}
}