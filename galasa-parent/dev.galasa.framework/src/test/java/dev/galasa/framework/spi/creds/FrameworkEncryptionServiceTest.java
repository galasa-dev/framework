/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import dev.galasa.framework.mocks.MockEnvironment;
import dev.galasa.framework.mocks.MockFileSystem;

public class FrameworkEncryptionServiceTest {

    private SecretKeySpec generateEncryptionKey() throws NoSuchAlgorithmException {
        byte[] keyBytes = RandomStringUtils.randomAlphanumeric(32).getBytes();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private String generateEncodedEncryptionKeyString() throws NoSuchAlgorithmException {
        byte[] keyBytes = generateEncryptionKey().getEncoded();
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    private String createEncryptionKeysYaml(String primaryEncryptionKey, List<String> oldEncryptionKeys) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("encryptionKey: " + primaryEncryptionKey);
        stringBuilder.append("\n");
        stringBuilder.append("fallbackDecryptionKeys: ");

        if (oldEncryptionKeys.isEmpty()) {
            stringBuilder.append("[]");
        } else {
            stringBuilder.append("\n");
            for (String key : oldEncryptionKeys) {
                stringBuilder.append("- " + key);
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    @Test
    public void testCanEncryptAndDecryptTextOk() throws Exception {
        // Given...
        SecretKeySpec key = generateEncryptionKey();
        MockFileSystem mockFileSystem = new MockFileSystem();
        MockEnvironment mockEnvironment = new MockEnvironment();
        FrameworkEncryptionService encryptionService = new FrameworkEncryptionService(key, mockFileSystem, mockEnvironment);
        String plainText = "encrypt me!";

        // When...
        String encryptedText = encryptionService.encrypt(plainText);
        assertThat(encryptedText).isNotNull();

        String decryptedText = encryptionService.decrypt(encryptedText);

        // Then...
        assertThat(decryptedText).isEqualTo(plainText);
    }

    @Test
    public void testCanLoadAndUseEncryptionKeysFromFileSystemOk() throws Exception {
        // Given...
        MockFileSystem mockFileSystem = new MockFileSystem();
        MockEnvironment mockEnvironment = new MockEnvironment();

        String mockEncryptionKeysFilePath = "/encryption-keys.yaml";
        mockEnvironment.setenv(FrameworkEncryptionService.ENCRYPTION_KEYS_PATH_ENV, mockEncryptionKeysFilePath);

        List<String> oldDecryptionKeys = new ArrayList<>();
        String encodedEncryptionkey = generateEncodedEncryptionKeyString();
        String yaml = createEncryptionKeysYaml(encodedEncryptionkey, oldDecryptionKeys);
        mockFileSystem.write(Paths.get(mockEncryptionKeysFilePath), yaml.getBytes(StandardCharsets.UTF_8));

        FrameworkEncryptionService encryptionService = new FrameworkEncryptionService(mockFileSystem, mockEnvironment);

        String plainText = "encrypt me!";

        // When...
        String encryptedText = encryptionService.encrypt(plainText);
        String decryptedText = encryptionService.decrypt(encryptedText);

        // Then...
        assertThat(decryptedText).isEqualTo(plainText);
    }

    @Test
    public void testDecryptTextWithWrongKeyReturnsNullText() throws Exception {
        // Given...
        MockFileSystem mockFileSystem = new MockFileSystem();
        MockEnvironment mockEnvironment = new MockEnvironment();

        String mockEncryptionKeysFilePath = "/encryption-keys.yaml";
        mockEnvironment.setenv(FrameworkEncryptionService.ENCRYPTION_KEYS_PATH_ENV, mockEncryptionKeysFilePath);

        List<String> oldDecryptionKeys = List.of(
            generateEncodedEncryptionKeyString(),
            generateEncodedEncryptionKeyString()
        );
        String encodedEncryptionkey = generateEncodedEncryptionKeyString();
        String yaml = createEncryptionKeysYaml(encodedEncryptionkey, oldDecryptionKeys);
        mockFileSystem.write(Paths.get(mockEncryptionKeysFilePath), yaml.getBytes(StandardCharsets.UTF_8));

        FrameworkEncryptionService encryptionService = new FrameworkEncryptionService(mockFileSystem, mockEnvironment);
        String mockEncryptedText = "letspretendthatthisisencrypted";

        // When...
        // The decryption should fail since the provided text was not encrypted with any known encryption keys
        String decryptedText = encryptionService.decrypt(mockEncryptedText);

        // Then...
        assertThat(decryptedText).isNull();
    }

    @Test
    public void testCreateEncryptionServiceFailsWhenNoKeysFileExists() throws Exception {
        // Given...
        MockFileSystem mockFileSystem = new MockFileSystem();
        MockEnvironment mockEnvironment = new MockEnvironment();

        String mockEncryptionKeysFilePath = "/encryption-keys.yaml";
        mockEnvironment.setenv(FrameworkEncryptionService.ENCRYPTION_KEYS_PATH_ENV, mockEncryptionKeysFilePath);

        // When...
        CredentialsException thrown = catchThrowableOfType(() -> {
            new FrameworkEncryptionService(mockFileSystem, mockEnvironment);
        }, CredentialsException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Failed to read encryption keys file");
    }

    @Test
    public void testEncryptTextWithNoKeyThrowsError() throws Exception {
        // Given...
        MockFileSystem mockFileSystem = new MockFileSystem();
        MockEnvironment mockEnvironment = new MockEnvironment();

        String mockEncryptionKeysFilePath = "/encryption-keys.yaml";
        mockEnvironment.setenv(FrameworkEncryptionService.ENCRYPTION_KEYS_PATH_ENV, mockEncryptionKeysFilePath);

        mockFileSystem.write(Paths.get(mockEncryptionKeysFilePath), null);

        FrameworkEncryptionService encryptionService = new FrameworkEncryptionService(mockFileSystem, mockEnvironment);
        String plainText = "encrypt me";

        // When...
        // The encryption should fail since the service does not have an encryption key to use
        CredentialsException thrown = catchThrowableOfType(() -> {
            encryptionService.encrypt(plainText);
        }, CredentialsException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Unable to encrypt the provided data");
    }
}
