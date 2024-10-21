/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

public class FrontendClient {

    @SerializedName("client-name")
    private String clientName;

    @SerializedName("last-login")
    private Instant lastLogin;

    // No-arg constructor
    public FrontendClient() {}

    // Parameterized constructor
    public FrontendClient(String clientName, Instant lastLoggedIn) {
        this.clientName = clientName;
        this.lastLogin = lastLoggedIn;
    }

    // Getter and Setter for lastLoggedIn
    public Instant getLastLoggedIn() {
        return lastLogin;
    }

    public void setLastLoggedIn(Instant lastLoggedIn) {
        this.lastLogin = lastLoggedIn;
    }

    // Getter and Setter for clientName
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    // toString method to display client details
    @Override
    public String toString() {
        return "Client [clientName=" + clientName + ", lastLoggedIn=" + lastLogin + "]";
    }
}
