/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
// package dev.galasa.framework.api.authentication.internal;

// import java.security.Principal;
// import java.util.Set;

// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletRequestWrapper;

// public class RequestWrapper extends HttpServletRequestWrapper {

//     private final String      username;
//     private final Set<String> roles;

//     public RequestWrapper(String username, Set<String> roles, HttpServletRequest request) {
//         super(request);

//         this.username = username;
//         this.roles = roles;
//     }

//     @Override
//     public Principal getUserPrincipal() {
//         return new Principal() {

//             @Override
//             public String getName() {
//                 return RequestWrapper.this.username;
//             }
//         };
//     }

//     @Override
//     public boolean isUserInRole(String role) {
//         return this.roles.contains(role);
//     }

// }
