/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.time.Instant;

import dev.galasa.framework.api.common.AuthToken;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.User;

public class MockInternalAuthToken implements IInternalAuthToken {

    private String tokenId;
    private String description;
    private String dexClientId;
    private Instant creationTime;
    private User owner;

    public MockInternalAuthToken(String tokenId, String description, Instant creationTime, User owner, String dexClientId) {
        this.tokenId = tokenId;
        this.description = description;
        this.dexClientId = dexClientId;
        this.creationTime = creationTime;
        this.owner = owner;
    }

    public MockInternalAuthToken(AuthToken tokenToCopy) {
        this.tokenId = tokenToCopy.getTokenId();
        this.description = tokenToCopy.getDescription();
        this.creationTime = tokenToCopy.getCreationTime();
        this.owner = tokenToCopy.getOwner();
    }

    @Override
    public String getTokenId() {
        return tokenId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getDexClientId() {
        return dexClientId;
    }

    @Override
    public Instant getCreationTime() {
        return creationTime;
    }

    @Override
    public User getOwner() {
        return owner;
    }
}
