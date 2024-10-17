/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.SystemEnvironment;

public class FrameworkEncryptionService implements IEncryptionService {

    public static final String ENCRYPTION_KEYS_PATH_ENV = "GALASA_ENCRYPTION_KEYS_PATH";

    private static final String KEY_ALGORITHM = "AES";
    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";

    private static final int GCM_AUTH_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_BYTES_LENGTH = 12;

    private SecretKeySpec encryptionKey;
    private List<SecretKeySpec> decryptionKeys = new ArrayList<>();

    private Environment environment = new SystemEnvironment();
    private IFileSystem fileSystem = new FileSystem();

    public FrameworkEncryptionService(SecretKeySpec encryptionKey) throws CredentialsException {
        this(encryptionKey, new FileSystem(), new SystemEnvironment());
    }

    public FrameworkEncryptionService(SecretKeySpec encryptionKey, IFileSystem fileSystem, Environment environment) throws CredentialsException {
        this.fileSystem = fileSystem;
        this.environment = environment;
        this.encryptionKey = encryptionKey;

        if (encryptionKey == null) {
            EncryptionKeys encryptionKeysYaml = parseEncryptionKeysFile();
            if (encryptionKeysYaml != null) {
                this.decryptionKeys = loadDecryptionKeys(encryptionKeysYaml);
                this.encryptionKey = loadPrimaryEncryptionKey(encryptionKeysYaml);
            }
        } else {
            this.decryptionKeys = new ArrayList<>();
            this.decryptionKeys.add(encryptionKey);
        }
    }

    public FrameworkEncryptionService(IFileSystem fileSystem, Environment environment) throws CredentialsException {
        this(null, fileSystem, environment);
    }

    private EncryptionKeys parseEncryptionKeysFile() throws CredentialsException {
        EncryptionKeys encryptionKeys = null;
        String encryptionKeysLocation = environment.getenv(ENCRYPTION_KEYS_PATH_ENV);
        if (encryptionKeysLocation != null) {
            try {
                String encryptionKeysYamlStr = fileSystem.readString(Paths.get(encryptionKeysLocation));
                if (encryptionKeysYamlStr != null && !encryptionKeysYamlStr.isBlank()) {
                    Yaml yamlParser = new Yaml(new Constructor(EncryptionKeys.class, new LoaderOptions()));
                    encryptionKeys = yamlParser.load(encryptionKeysYamlStr);
                }
            } catch (Exception e) {
                throw new CredentialsException("Failed to read encryption keys file", e);
            }
        }

        return encryptionKeys;
    }

    private SecretKeySpec loadPrimaryEncryptionKey(EncryptionKeys encryptionKeysYaml) throws CredentialsException {
        SecretKeySpec encryptionKey = null;
        String encodedKeyStr = encryptionKeysYaml.getEncryptionKey();
        if (encodedKeyStr != null) {
            byte[] decodedPrimaryKeyBytes = Base64.getDecoder().decode(encodedKeyStr);
            encryptionKey = new SecretKeySpec(decodedPrimaryKeyBytes, KEY_ALGORITHM);
        }
        return encryptionKey;
    }

    private List<SecretKeySpec> loadDecryptionKeys(EncryptionKeys encryptionKeysYaml) throws CredentialsException {
        List<SecretKeySpec> decryptionKeys = new ArrayList<>();

        // The encryption keys secret is mounted as a file of the form:
        //   encryptionKey: <current-base64-encoded-key>
        //   fallbackDecryptionKeys:
        //   - <encoded-fallback-key-1>
        //   - <encoded-fallback-key-2>
        SecretKeySpec primaryEncryptionKey = loadPrimaryEncryptionKey(encryptionKeysYaml);
        if (primaryEncryptionKey != null) {
            decryptionKeys.add(primaryEncryptionKey);
        }

        for (String oldKey : encryptionKeysYaml.getFallbackDecryptionKeys()) {
            byte[] oldKeyDecodedBytes = Base64.getDecoder().decode(oldKey);
            SecretKeySpec key = new SecretKeySpec(oldKeyDecodedBytes, KEY_ALGORITHM);
            decryptionKeys.add(key);
        }
        return decryptionKeys;
    }

    @Override
    public String encrypt(String plainText) throws CredentialsException {
        if (this.encryptionKey == null) {
            throw new CredentialsException("Unable to encrypt the provided data. No encryption key has been set");
        }

        // Generate a random initialization vector (IV)
        byte[] initVector = new byte[GCM_IV_BYTES_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(initVector);

        byte[] encryptedIvAndText;
        try {
            // Initialise the GCM cipher in encrypt mode
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_AUTH_TAG_LENGTH_BITS, initVector);
            cipher.init(Cipher.ENCRYPT_MODE, this.encryptionKey, gcmParameterSpec);

            // Encrypt the plaintext
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Concatenate the IV and the encrypted text
            encryptedIvAndText = new byte[initVector.length + encryptedBytes.length];
            System.arraycopy(initVector, 0, encryptedIvAndText, 0, initVector.length);
            System.arraycopy(encryptedBytes, 0, encryptedIvAndText, initVector.length, encryptedBytes.length);

        } catch (Exception e) {
            throw new CredentialsException("Failed to encrypt the provided data", e);
        }
        return Base64.getEncoder().encodeToString(encryptedIvAndText);
    }

    @Override
    public String decrypt(String encryptedText) throws CredentialsException {
        String decryptedText = null;
        for (SecretKeySpec key : decryptionKeys) {
            try {
                decryptedText = decrypt(encryptedText, key);
            } catch (CredentialsException e) {
                // Decryption failed, so let's try the next key...
            }
        }
        return decryptedText;
    }

    private String decrypt(String encryptedText, SecretKeySpec decryptionKey) throws CredentialsException {
        String decryptedText = null;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

            // Get the IV from the encrypted text
            byte[] initVector = new byte[GCM_IV_BYTES_LENGTH];
            System.arraycopy(decodedBytes, 0, initVector, 0, initVector.length);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_AUTH_TAG_LENGTH_BITS, initVector);
            cipher.init(Cipher.DECRYPT_MODE, decryptionKey, gcmParameterSpec);

            // Strip off the IV and decrypt the text
            byte[] decryptedBytes = cipher.doFinal(decodedBytes, initVector.length, decodedBytes.length - initVector.length);
            decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CredentialsException("Failed to decrypt the provided data", e);
        }
        return decryptedText;
    }
}
