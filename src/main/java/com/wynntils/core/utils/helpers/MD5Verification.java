/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.core.utils.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.regex.Pattern;

public class MD5Verification {

    String md5;

    public MD5Verification(File f) {
        try {
            InputStream fis = new FileInputStream(f);

            byte[] buffer = new byte[1024];
            MessageDigest md = MessageDigest.getInstance("MD5");
            int numRead = fis.read(buffer);

            while (numRead != -1) {
                if (numRead > 0) {
                    md.update(buffer, 0, numRead);
                }
                numRead = fis.read(buffer);
            }

            fis.close();

            md5 = hexDigest(md.digest());
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public MD5Verification(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);

            md5 = hexDigest(md.digest());
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private static final char[] hex = "0123456789abcdef".toCharArray();
    private static String hexDigest(byte[] digest) {
        char[] hexChars = new char[32];
        for (int i = 0; i < 16; ++i) {
            int b = Byte.toUnsignedInt(digest[i]);
            hexChars[2 * i] = hex[b >> 4];
            hexChars[2 * i + 1] = hex[b & 15];
        }
        return new String(hexChars);
    }

    public String getMd5() {
        return md5;
    }

    public boolean equals(String other) {
        return md5 != null && md5.equalsIgnoreCase(other);
    }

    private static final Pattern md5Regex = Pattern.compile("^[0-9a-fA-F]{32}$");

    public static boolean isMd5Digest(String s) {
        return s != null && s.length() == 32 && md5Regex.matcher(s).matches();
    }

}
