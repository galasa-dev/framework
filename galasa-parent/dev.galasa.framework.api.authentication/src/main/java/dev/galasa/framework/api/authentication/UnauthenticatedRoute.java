/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum UnauthenticatedRoute {

    AUTH("/auth", "GET", "POST"),
    AUTH_TOKENS("/auth/tokens", "POST"),
    AUTH_CALLBACK("/auth/callback", "GET"),
    BOOTSTRAP("/bootstrap", "GET"),
    BOOTSTRAP_EXTERNAL("/bootstrap/external", "GET"),
    HEALTH("/health", "GET"),
    ;

    private String route;
    private List<String> allowedMethods;

    private UnauthenticatedRoute(String route, String... allowedMethods) {
        this.route = route;
        this.allowedMethods = Arrays.asList(allowedMethods);
    }

    public static Map<String, List<String>> getRoutesAsMap() {
        Map<String, List<String>> routeMap = new HashMap<>();
        for (UnauthenticatedRoute route : values()) {
            routeMap.put(route.toString(), route.getAllowedMethods());
        }
        return routeMap;
    }

    @Override
    public String toString() {
        return this.route;
    }

    public List<String> getAllowedMethods() {
        return this.allowedMethods;
    }
}
