package com.nex.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class EncryptUtil {

    private static final int SALT_SIZE = 1;

    public String getSalt() {

        // 1. Random, byte 객체 생성
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];

        // 2. 난수 생성
        random.nextBytes(salt);

        // 3. byte To String (10진수의 문자열로 변경)
        StringBuffer sb = new StringBuffer();
        for(byte b : salt) {
            sb.append(String.format("%02x", b));
        };

        return sb.toString();
    }

    public String getEncrypt(String pwd, String salt) {
        String result = "";

        try {
            // 1. SHA256 알고리즘 객체 생성
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // 2. pwd오 salt 합친 문자열에 SHA-256 적용
            md.update((pwd+salt).getBytes());
            byte[] pwdsalt = md.digest();

            // 3. byte To String (10진수의 문자열로 변경)
            StringBuffer sb = new StringBuffer();
            for(byte b : pwdsalt) {
                sb.append(String.format("%02x", b));
            }

            result = sb.toString() + salt;

        } catch (NoSuchAlgorithmException e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
        }

        return result;
    }

    public boolean matches(String plainText, String encryptedText) {
        // 평문 텍스트와 디비에 저장되어 있는 암호화된 텍스트를 비교
        if(encryptedText.equals(getEncrypt(plainText, encryptedText.substring(64)))) {
            return true;
        } else {
            return false;
        }
    }
}
