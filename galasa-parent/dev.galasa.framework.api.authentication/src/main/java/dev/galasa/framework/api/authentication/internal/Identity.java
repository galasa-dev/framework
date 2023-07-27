// /*
//  * Licensed Materials - Property of IBM
//  *
//  * (c) Copyright IBM Corp. 2019.
//  */
// package dev.galasa.framework.api.authentication.internal;

// import java.io.IOException;

// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;

// import org.osgi.service.component.annotations.Reference;

// import com.auth0.jwt.JWT;
// import com.auth0.jwt.exceptions.JWTDecodeException;

// import dev.galasa.framework.spi.IFramework;

// //@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = ("osgi.http.whiteboard.servlet.pattern=/auth/identity"), name = "Galasa Identity")
// public class Identity extends HttpServlet {
//    private static final long serialVersionUID = 1L;

//     @Reference
//     public IFramework framework; // NOSONAR

//     @Override
//     public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//         response.setContentType("text/plain");

//         String jwt = getBearerToken(request);
//         String subject;

//         try {
//             subject = JWT.decode(jwt).getSubject();
//         } catch (JWTDecodeException e) {
//             response.setStatus(500);
//             response.addHeader("WWW-Authenticate", "Basic realm=\"Galasa\""); // *** Ability to set the realm
//             response.getWriter().write("Failed to decode token");// NOSONAR //TODO catch this as SQ says
//             return;
//         }

//         if (request.isUserInRole("admin")) {
//             try {
//                 response.getWriter().write(subject + " is in admin\n");
//             } catch (IOException e) {
//                 response.setStatus(500);
//                 response.addHeader("WWW-Authenticate", "Basic realm=\"Galasa\""); // *** Ability to set the realm
//                 response.getWriter().write("Failed to write");// NOSONAR //TODO catch this as SQ says
//                 return;
//             }
//             return;
//         }
//         if (request.isUserInRole("user")) {
//             try {
//                 response.getWriter().write(subject + " is a user\n");
//             } catch (IOException e) {
//                 response.setStatus(500);
//                 response.addHeader("WWW-Authenticate", "Basic realm=\"Galasa\""); // *** Ability to set the realm
//                 response.getWriter().write("Failed to write");// NOSONAR //TODO catch this as SQ says
//                 return;
//             }
//             return;
//         }

//         response.addHeader("WWW-Authenticate", "Basic realm=\"Galasa\""); // *** Ability to set the realm
//         response.getWriter().write("No authetication!\n");// NOSONAR
//         return;
//     }

//     private String getBearerToken(HttpServletRequest request) {
//         String authHeader = request.getHeader("Authorization");
//         if (authHeader != null && authHeader.startsWith("Bearer")) {
//             return authHeader.substring("bearer".length());
//         }
//         return null;
//     }
// }
