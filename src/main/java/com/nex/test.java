package com.nex;


import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.registry.AlgorithmRegistry;
import org.jasypt.salt.StringFixedSaltGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class test {


    public static void jastypt(String str) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

        encryptor.setAlgorithm("PBEWITHMD5ANDTRIPLEDES");
        encryptor.setPassword("test_password");

        //ADD Salt
        //saltGenerator를 지정하지 않으면 RandomSaltGenerator를 default로 사용합니다.
        //random salt를 사용하는 경우는 암호화된 결과 값이 매번 바뀌므로 권장합니다
        //encryptor.setSaltGenerator(new StringFixedSaltGenerator("someFixedSalt"));

        String encryption = encryptor.encrypt(str);
        String decryption = encryptor.decrypt(encryption);

        log.debug("암호화: {}",encryption);
        log.debug("복호화: {}",decryption);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        jastypt("veneas");

    }
}
