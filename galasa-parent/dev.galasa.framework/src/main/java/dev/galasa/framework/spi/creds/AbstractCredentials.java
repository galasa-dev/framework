/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.time.Instant;
import java.util.Properties;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.ICredentials;

/**
 * An abstract class where common credential-related details are stored.
 */
public abstract class AbstractCredentials extends Credentials implements ICredentials {

    protected static final String CREDS_PROPERTY_PREFIX = "secure.credentials.";

    private String description;
    private String lastUpdatedByUser;
    private Instant lastUpdatedTime;

    public AbstractCredentials(SecretKeySpec key) throws CredentialsException {
        super(key);
    }

    public AbstractCredentials() {
        super();
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setLastUpdatedByUser(String username) {
        this.lastUpdatedByUser = username;
    }

    @Override
    public void setLastUpdatedTime(Instant time) {
        this.lastUpdatedTime = time;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLastUpdatedByUser() {
        return lastUpdatedByUser;
    }

    @Override
    public Instant getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    @Override
    public Properties getMetadataProperties(String credentialsId) {
        Properties properties = new Properties();
        if (description != null) {
            properties.put(CREDS_PROPERTY_PREFIX + credentialsId + ".description", description);
        }

        if (lastUpdatedTime != null) {
            properties.put(CREDS_PROPERTY_PREFIX + credentialsId + ".lastUpdated.time", lastUpdatedTime.toString());
        }

        if (lastUpdatedByUser != null) {
            properties.put(CREDS_PROPERTY_PREFIX + credentialsId + ".lastUpdated.user", lastUpdatedByUser);
        }
        return properties;
    }
}
