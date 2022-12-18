package com.example.demo2.common.encryption;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.util.Base64;

@Service
public class LongTextEncryptor {
    private AttributeEncryptor encryptor;

    @Autowired
    public LongTextEncryptor(AttributeEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public String encrypt(String rawText) {
        StringBuilder encryptedText = new StringBuilder();
        int maxLen = rawText.length();
        int beginIdx = 0;
        int endIdx = Math.min(64, maxLen-1);

        while (beginIdx<maxLen) {
            encryptedText.append(
                    encryptor.convertToDatabaseColumn(rawText.substring(beginIdx, endIdx)
                    ));
            beginIdx = endIdx;
            endIdx = Math.min(maxLen, beginIdx+64);
            System.out.println(beginIdx+" "+endIdx+" "+maxLen);
            encryptedText.append("-");
        }

        System.out.println("encryptedText = " + encryptedText);
        return String.valueOf(encryptedText);

    }

    public String decrypt(String encryptedText) {
        int maxLen = encryptedText.length();
        int idx = 0;
        StringBuilder res = new StringBuilder();
        while (idx<maxLen) {
            StringBuilder temp = new StringBuilder();
            while (encryptedText.charAt(idx) != '-') {
                temp.append(encryptedText.charAt(idx));
                idx+=1;
            }
            res.append(new String(encryptor.convertToEntityAttribute(String.valueOf(temp))));
            idx+=1;
        }
        return String.valueOf(res);
    }
}
