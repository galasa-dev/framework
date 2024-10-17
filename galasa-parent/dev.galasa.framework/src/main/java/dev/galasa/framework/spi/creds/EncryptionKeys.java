/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.util.List;

public class EncryptionKeys {
    private String encryptionKey;
    private List<String> fallbackDecryptionKeys;

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public List<String> getFallbackDecryptionKeys() {
        return fallbackDecryptionKeys;
    }

    public void setFallbackDecryptionKeys(List<String> fallbackDecryptionKeys) {
        this.fallbackDecryptionKeys = fallbackDecryptionKeys;
    }
}
