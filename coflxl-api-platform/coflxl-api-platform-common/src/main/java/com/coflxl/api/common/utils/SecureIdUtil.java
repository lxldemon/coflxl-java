package com.coflxl.api.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class SecureIdUtil {
    // 16 bytes key
    private static final String KEY = "CoflxlSecureKey@";

    public static String encrypt(Long id) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(String.valueOf(id).getBytes());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
    }

    public static Long decrypt(String text) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded = Base64.getUrlDecoder().decode(text);
        byte[] original = cipher.doFinal(decoded);
        return Long.valueOf(new String(original));
    }
}
