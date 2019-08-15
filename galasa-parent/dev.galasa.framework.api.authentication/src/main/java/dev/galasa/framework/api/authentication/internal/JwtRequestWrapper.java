package dev.galasa.framework.api.authentication.internal;

import java.security.Principal;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class JwtRequestWrapper extends HttpServletRequestWrapper {
	
	private final String username;
	private final String role;

	public JwtRequestWrapper(String username, String role, HttpServletRequest request) {
		super(request);
		
		this.username = username;
		this.role    = role;
	}

	@Override
	public Principal getUserPrincipal() {
		return new Principal() {
			
			@Override
			public String getName() {
				return JwtRequestWrapper.this.username;
			}
		};
	}
	
	@Override
	public boolean isUserInRole(String role) {
		return this.role.equals(role);
	}
	
}
