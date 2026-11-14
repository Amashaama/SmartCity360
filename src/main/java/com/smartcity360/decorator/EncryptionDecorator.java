package com.smartcity360.decorator;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionDecorator extends DataStreamDecorator {

    private static final String ALGORITHM = "AES";
    private static final byte[] KEY_BYTES = "SmartCity360Key!".getBytes(StandardCharsets.UTF_8);

    public EncryptionDecorator(DataStream wrappedStream) {
        super(wrappedStream);
    }

    @Override
    public void writeData(String data) {
        wrappedStream.writeData(encrypt(data));
    }

    @Override
    public String readData() {
        return decrypt(wrappedStream.readData());
    }


    public static String encrypt(String text) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY_BYTES, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("AES encryption failed", e);
        }
    }

    public static String decrypt(String text) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY_BYTES, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(text);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("AES decryption failed", e);
        }
    }
}


