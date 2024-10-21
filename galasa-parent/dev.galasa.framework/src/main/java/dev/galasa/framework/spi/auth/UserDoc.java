/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class UserDoc {

    @SerializedName("_id")
    private String userNumber;

    @SerializedName("_rev")
    private String version;

    @SerializedName("login-id")
    private String loginId;

    @SerializedName("activity")
    private List<FrontendClient> clients;

    public UserDoc(String loginId, List<FrontendClient> clients) {
        this.loginId = loginId;
        this.clients = clients;
    }

    public UserDoc(String loginId) {
        this.loginId = loginId;
    }

    public String getUserNumber(){
        return userNumber;
    }

    public void setUserNumber(String userNumber){
        this.userNumber = userNumber;
    }

    public String getVersion(){
        return version;
    }

    public void setVersion(String version){
        this.version = version;
    }

    // Getter for loginId
    public String getLoginId() {
        return loginId;
    }

    // Setter for loginId
    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    // Getter for clients
    public List<FrontendClient> getClients() {
        return clients;
    }

    // Setter for clients
    public void setClients(List<FrontendClient> clients) {
        this.clients = clients;
    }
    
}
