package com.github.njuro.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.github.njuro.helpers.Constants.TRIPCODE_LENGTH;
import static com.github.njuro.helpers.Constants.TRIPCODE_SEPARATOR;

public class Tripcode {

    public static String generateTripcode(String password) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Hashing failed", e);
        }

        md.update(password.getBytes());
        byte byteData[] = md.digest();

        StringBuffer tripcode = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            tripcode.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return TRIPCODE_SEPARATOR + tripcode.substring(tripcode.length() - TRIPCODE_LENGTH);
    }

}
