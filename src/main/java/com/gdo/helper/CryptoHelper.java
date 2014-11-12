/**
 * Copyright GDO - 2004
 */
package com.gdo.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Key;

import javax.crypto.Cipher;

/**
 * <p>
 * Helpers on crypto.
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
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class CryptoHelper {

    private CryptoHelper() {
        // utility class, disable instanciation
    }

    public static byte[] encrypt(Key key, String input) throws Exception {
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] b = input.getBytes();
        return cipher.doFinal(b);

    }

    public static String decrypt(Key key, byte[] input) throws Exception {
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] b = cipher.doFinal(input);
        return new String(b);
    }

    /**
     * Serializes an object into a file (a key for example).
     * 
     * @param obj
     *            the serializable object to store.
     * @param file
     *            file which will contains the serialization.
     */
    public static void serialize(Serializable obj, File file) throws Exception {
        FileOutputStream fo = new FileOutputStream(file);
        ObjectOutputStream out = new ObjectOutputStream(fo);
        out.writeObject(obj);
        out.close();
    }

    /**
     * Retrieves an serializable object from a file (a key for example).
     * 
     * @param file
     *            file containing the object serialization.
     * @return the object serialized.
     */
    public static Object deserialize(File file) throws Exception {
        FileInputStream fi = new FileInputStream(file);
        ObjectInputStream in = new ObjectInputStream(fi);
        Object obj = in.readObject();
        in.close();
        return obj;
    }

    /**
     * Convertit des octets en leur representation hexadecimale (base 16),
     * chacun se retrouvant finalement 'non signe' et sur 2 caracteres.
     * 
     * @see http 
     *      ://java.sun.com/developer/technicalArticles/Security/AES/AES_v1.html
     */
    public static String byteToHex(byte[] bits) {
        if (bits == null) {
            return null;
        }
        StringBuffer hex = new StringBuffer(bits.length * 2); // encod(1_bit) =>
        // 2 digits
        for (int i = 0; i < bits.length; i++) {
            if ((bits[i] & 0xff) < 0x10) { // 0 < .. < 9
                hex.append("0");
            }
            hex.append(Integer.toString(bits[i] & 0xff, 16)); // [(bit+256)%256]^16
        }
        return hex.toString();
    }

    /**
     * Convertit une representation d'hexadecimaux (base 16) en octets, chacun
     * �tant initialement 'non signe' et sur 2 caracteres.
     * 
     * @see http://forum.java.sun.com/thread.jspa?threadID=659432
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
}