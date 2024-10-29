/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

import java.time.Instant;
import java.util.Properties;

public interface ICredentials {
    Properties toProperties(String credentialsId);
    Properties getMetadataProperties(String credentialsId);

    void setDescription(String description);
    void setLastUpdatedByUser(String username);
    void setLastUpdatedTime(Instant time);

    String getDescription();
    String getLastUpdatedByUser();
    Instant getLastUpdatedTime();
}
