package dev.voras.framework.api.authentication.internal;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
		service=Filter.class,
		scope=ServiceScope.PROTOTYPE,
		property = {"osgi.http.whiteboard.filter.pattern=/auth"},
		//		configurationPid= {"dev.voras"},
		//		configurationPolicy=ConfigurationPolicy.REQUIRE,
		name="Voras Basic Auth"
		)
public class BasicAuthFilter implements Filter {
	
	private final Log logger = LogFactory.getLog(getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (!(request instanceof HttpServletRequest)) {
			chain.doFilter(request, response);
			return;
		}

		HttpServletRequest servletRequest = (HttpServletRequest) request;
		HttpServletResponse servletResponse = (HttpServletResponse) response;

		Principal principal = servletRequest.getUserPrincipal();
		if (principal != null) {  // already authenticated
			chain.doFilter(request, response);
			return;
		}

		String authorization = servletRequest.getHeader("Authorization");
		if (authorization == null) {
			chain.doFilter(request, response);
			return;
		}

		StringTokenizer st = new StringTokenizer(authorization);
		if (!st.hasMoreTokens()) {
			chain.doFilter(request, response);
			return;
		}

		String basic = st.nextToken();
		if (!"basic".equalsIgnoreCase(basic)) {
			chain.doFilter(request, response);
			return;
		}

		if (!st.hasMoreTokens()) {
			chain.doFilter(request, response);
			return;
		}

		String credentials = new String(Base64.getDecoder().decode(st.nextToken()));
		String[] parts = credentials.split(":");
		if (parts.length != 2) {
			invalidAuth(servletRequest, servletResponse);
			return;
		}

		final String username = parts[0].trim();
		final String password = parts[1].trim();

		CallbackHandler callbackHandler = new CallbackHandler() {

			@Override
			public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
				for(Callback callback : callbacks) {
					if (callback instanceof NameCallback) {
						NameCallback nc = (NameCallback) callback;
						nc.setName(username);
					} else if (callback instanceof PasswordCallback) {
						PasswordCallback pc = (PasswordCallback) callback;
						pc.setPassword(password.toCharArray());
					} 
				}
			}
		};

		Subject subject = null;
		try {
			LoginContext ctx = new LoginContext("voras", callbackHandler); // TODO set realm
			ctx.login();
			subject = ctx.getSubject();
		} catch(LoginException e) {
			invalidAuth(servletRequest, servletResponse);
			logger.info("Authentication failed for user '" + username + "'", e);
			return;
		}
		
		if (subject == null) {
			invalidAuth(servletRequest, servletResponse);
			return;
		}
		
		String name = null;
		HashSet<String> roles = new HashSet<>();
		
		for(Principal p : subject.getPrincipals()) {
			String pName = p.getClass().getName();
			if (pName.endsWith(".UserPrincipal")) {  // TODO got to be a better way to do this
				name = p.getName();
			} else if (pName.endsWith(".RolePrincipal")) {
				roles.add(p.getName());
			}
		}
		
		if (name == null) {
			name = username;
		}
		RequestWrapper wrapper = new RequestWrapper(name.toLowerCase(), roles, servletRequest);
		chain.doFilter(wrapper, response);
	}

	private void invalidAuth(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
		servletResponse.setStatus(401);
		servletResponse.addHeader("WWW-Authenticate", "Basic realm=\"Voras\"");  //*** Ability to set the realm
		servletResponse.getWriter().write("Invalid authentication");
		return;
	}

	@Override
	public void destroy() {
	}

}
