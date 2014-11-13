/*
 * Copyright GDO - 2004
 */
package com.gdo.helper;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * <p>
 * Helper for convertion between standard classes.
 * </p>
 * 
 * <p>
 * Used to trace errors and allow default value.
 * </p>
 * 
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class ConverterHelper {

    private static String SECURITY_ALGORITHM = "DES";

    private ConverterHelper() {
        // utility class, disable instanciation
    }

    public static Integer stringToInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return new Integer(0);
        }
    }

    public static Byte stringToByte(String value) {
        try {
            return Byte.valueOf(value);
        } catch (Exception e) {
            return new Byte((byte) 0);
        }
    }

    /**
     * Parses the string argument as a boolean. The boolean returned represents
     * the value <tt>true</tt> if the string argument is not null and is equal,
     * ignoring case, to the usual accepting true strings "true", "yes", "ok",
     * "vrai", ... <br>
     * <br>
     * Example: Boolean.parseBoolean("True") returns true. <br>
     * Example: Boolean.parseBoolean("yes") returns true.
     * 
     * @param value
     *            the string value.
     * @return the boolean value.
     */
    public static Boolean parseBoolean(String value) {
        if (StringUtils.isBlank(value))
            return Boolean.FALSE;
        String tested = value.toLowerCase();
        if ("true".equals(tested))
            return Boolean.TRUE;
        if ("vrai".equals(tested))
            return Boolean.TRUE;
        if ("ok".equals(tested))
            return Boolean.TRUE;
        if ("yes".equals(tested))
            return Boolean.TRUE;
        if ("oui".equals(tested))
            return Boolean.TRUE;
        if ("y".equals(tested))
            return Boolean.TRUE;
        if ("o".equals(tested))
            return Boolean.TRUE;
        if ("1".equals(tested))
            return Boolean.TRUE;
        if ("on".equals(tested))
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    public static Key getKey() {
        try {
            return KeyGenerator.getInstance(ConverterHelper.SECURITY_ALGORITHM).generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Encrypt a string into a byte array.
     * 
     * @param key
     * @param input
     * @return
     */
    public static byte[] encrypt(Key key, String input) {
        try {
            Cipher cipher = Cipher.getInstance(ConverterHelper.SECURITY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] b = input.getBytes();
            return cipher.doFinal(b);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(Key key, byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance(ConverterHelper.SECURITY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] b = cipher.doFinal(input);
            return new String(b);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert an octets array in a string of hexadecimal number. Each byte is
     * represented by two characters and not signed.
     */
    public static String byteToHex(byte[] bits) {
        if (bits == null) {
            return null;
        }

        StringBuffer hex = new StringBuffer(bits.length * 2); // encod(1_bit) =>
        // 2 digits
        for (byte element : bits) {
            if ((element & 0xff) < 0x10) { // 0 < .. < 9
                hex.append("0");
            }
            hex.append(Integer.toString(element & 0xff, 16)); // [(bit+256)%256]^16
        }
        return hex.toString();
    }

    /**
     * Convert a string of hexadecimal number in an octets array. Each byte is
     * represented by two characters and not signed.
     */
    public static byte[] hexToByte(String bits) {
        if ((bits == null) || (bits.length() % 2 != 0)) { // pair [xy]
            return null;
        }

        byte[] bytes = new byte[bits.length() / 2]; // decod(2_digits) => 1 bit
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(bits.substring(2 * i, 2 * i + 2), 16); // |bit|^16
        }
        return bytes;
    }

    // "yyyy-MM-dd HH:mm:ss"
    public static String dateConverter(String date, String formatRead, String formatWrite) {
        try {

            // specific cases
            if ("00/00/0000".equals(date) && "dd/MM/yyyy".equals(formatRead) && "yyyy-MM-dd".equals(formatWrite))
                return "0000-00-00";

            // translation
            DateFormat dateFormatRead = new SimpleDateFormat(formatRead);
            DateFormat dateFormatWrite = new SimpleDateFormat(formatWrite);
            Date parse = dateFormatRead.parse(date);
            String value = dateFormatWrite.format(parse);
            return value;
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * Return the number of minutes in a time string.
     * 
     * @param time
     * @return
     */
    public static int timeToMinutes(String time) {
        int index = time.indexOf(':');
        if (index == -1) {
            return Integer.parseInt(time) * 60;
        }
        int last = time.lastIndexOf(':');
        if (last == -1 || index == last) {
            return Integer.parseInt(time.substring(0, index)) * 60 + Integer.parseInt(time.substring(index + 1));
        }
        return Integer.parseInt(time.substring(0, index)) * 60 + Integer.parseInt(time.substring(index + 1, last));
    }

    /**
     * Formats a date into a specific pattern.
     * 
     * @param date
     *            the date to be formatted.
     * @param format
     *            the pattern to use to format the date.
     * @return the formatted date.
     */
    public static String dateToString(Date date, String format) {
        try {
            return DateFormatUtils.format(date, format);
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * Formats a time into a specific pattern.
     * 
     * @param time
     *            the time to be formatted.
     * @param format
     *            the pattern to use to format the time.
     * @return the formatted time.
     */
    public static String timeToString(Time time, String format) {
        try {
            return new SimpleDateFormat(format).format(time);
        } catch (Exception e) {
        }
        return "";
    }

}
