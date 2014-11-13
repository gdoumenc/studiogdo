/**
 * Copyright GDO - 2004
 */
package com.gdo.helper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * Helpers on string.
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
 * @author Guillaume Doumenc (<a>>href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class StringHelper {

    // indexes of accentuated characters
    private static int FIRST = 192;
    private static int LAST = 255;
    private static List<String> _charMap = new ArrayList<String>();

    public static Class<String> CLASS_STRING = String.class;
    public static String EMPTY_STRING = "";
    public static StringBuffer EMPTY_STRING_BUFFER = new StringBuffer(StringHelper.EMPTY_STRING);
    public static Reader EMPTY_STRING_READER = new StringReader(EMPTY_STRING);
    public static InputStream EMPTY_STRING_INPUT_STREAM = new ByteArrayInputStream(EMPTY_STRING.getBytes());
    public static String[] EMPTY_STRINGS = new String[0];
    public static List<String> EMPTY_STRINGS_LIST = new ArrayList<String>();
    public static Map<String, String> EMPTY_STRINGS_MAP = new ConcurrentHashMap<String, String>();

    public static char NEW_LINE = '\n';
    public static char DOT = '.';
    public static char UNDERSCORE = '_';

    static {
        StringHelper.initCharMap();
    }

    private StringHelper() {
        // utility class, disable instanciation
    }

    public static String escapeSql(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        str = StringUtils.replace(str, "\\", "\\\\");
        return StringUtils.replace(str, "'", "''");
    }

    /**
     * Adds a string or more to an array of string.
     * 
     * @return the concatenated array of two arrays.
     */
    public static String[] add(String[] array1, String... array2) {
        String[] concat = new String[array1.length + array2.length];
        int i = 0;
        for (String s : array1) {
            concat[i++] = s;
        }
        for (String s : array2) {
            concat[i++] = s;
        }
        return concat;
    }

    /**
     * Converts the specified string into a valid Java identifier. All illegal
     * characters are replaced by underscores.
     * 
     * @param aString
     *            The string must contain at least one character.
     * @return <i>(required)</i>.
     */
    public static String toJavaIdentifier(String str) {
        StringBuffer res = new StringBuffer();
        int idx = 0;
        char c = str.charAt(idx);
        if (Character.isJavaIdentifierStart(c)) {
            res.append(c);
            idx++;
        } else if (Character.isJavaIdentifierPart(c)) {
            res.append('_');
        }
        while (idx < str.length()) {
            c = str.charAt(idx++);
            res.append(Character.isJavaIdentifierPart(c) ? c : '_');
        }
        return res.toString();
    }

    public static String read(Reader in) {
        if (in == null)
            return "";

        try {
            StringWriter sw = new StringWriter();
            int c;
            while ((c = in.read()) != -1) {
                sw.write(c);
            }
            in.close();
            return sw.getBuffer().toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static char getLastChar(String str) {
        return str.charAt(str.length() - 1);
    }

    public static int indexOf(String str, char c) {
        if (StringUtils.isEmpty(str))
            return -1;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (c == str.charAt(i))
                return i;
        }
        return -1;
    }

    public static String[] splitShortString(String str, char separator) {
        if (StringUtils.isEmpty(str)) {
            return StringHelper.EMPTY_STRINGS;
        }

        int len = str.length();
        int lastTokenIndex = 0;

        // Step 1: how many substrings?
        // We exchange double scan time for less memory allocation
        for (int pos = str.indexOf(separator); pos >= 0; pos = str.indexOf(separator, pos + 1)) {
            lastTokenIndex++;
        }

        // Step 2: allocate exact size array
        String[] list = new String[lastTokenIndex + 1];

        int oldPos = 0;

        // Step 3: retrieve substrings
        for (int pos = str.indexOf(separator), i = 0; pos >= 0; pos = str.indexOf(separator, (oldPos = (pos + 1)))) {
            list[i++] = substring(str, oldPos, pos);
        }

        list[lastTokenIndex] = substring(str, oldPos, len);

        return list;
    }

    public static String[] splitShortStringAndTrim(String str, char separator) {
        return trim(splitShortString(str, separator));
    }

    public static String concat(String[] array, char separator) {
        StringBuffer buffer = new StringBuffer();
        for (String str : array) {
            if (buffer.length() > 0)
                buffer.append(separator);
            buffer.append(str);
        }
        return buffer.toString();
    }

    public static String substring(String str, int begin, int end) {
        if (begin >= end)
            return EMPTY_STRING;
        return str.substring(begin, end);
    }

    /**
     * Removes n characters from the end of the string.
     * 
     * @param length
     *            number of characters to be removed.
     */
    public static String substringEnd(String str, int length) {
        if (length >= str.length())
            return EMPTY_STRING;
        return str.substring(0, str.length() - length);
    }

    /**
     * Trims all the strings contained in an array.
     * 
     * @param strings
     *            strings array
     */
    public static String[] trim(String[] strings) {
        if (!ClassHelper.isEmpty(strings)) {
            int len = strings.length;
            for (int i = 0; i < len; i++) {
                strings[i] = strings[i].trim();
            }
        }
        return strings;
    }

    /**
     * Replaces a text in a string (the text is not a regular expression).
     * 
     * @return the initial string with the text replaced.
     * @param string
     *            initial string.
     * @param text
     *            text to be replaced (not a regular expression).
     * @param replace
     *            replacement text.
     */
    public static String replaceAll(String string, String text, String replace) {
        if (StringUtils.isEmpty(string))
            return string;
        if (StringUtils.isEmpty(text))
            return string;

        int len = string.length();
        int lastIndex = 0;

        int index = string.indexOf(text, lastIndex);
        if (index == -1)
            return string;

        StringBuffer strBuf = new StringBuffer();
        while (index != -1 && index < len) {
            strBuf.append(string.substring(lastIndex, index));
            strBuf.append(replace);
            lastIndex = index + text.length();
            index = string.indexOf(text, lastIndex);
        }
        if (lastIndex != 0)
            strBuf.append(string.substring(lastIndex));

        return strBuf.toString();
    }

    public static void replaceAll(StringBuffer string, char ch1, String replace) {
        int len = string.length();
        for (int i = 0; i < len; i++) {
            if (string.charAt(i) == ch1) {
                string.replace(i, i + 1, replace);
            }
        }
    }

    public static void append(StringBuffer string, String text, int pos, int len) {
        for (int i = pos; i < len; i++) {
            string.append(text.charAt(i));
        }
    }

    /**
     * Removes all accent character or spaces from a string.
     * 
     * @param str
     *            the string to be normalized.
     * @return the string without spaces or accent.
     */
    public static String withoutAccentOrSpace(String str) {

        // removes all spaces
        String s = str.replaceAll("\\s+", "_");

        // removed
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("");
    }

    public static String withoutAccent(String str) {
        StringBuffer buffer = new StringBuffer(str);

        for (int bcl = 0; bcl < buffer.length(); bcl++) {
            int car = str.charAt(bcl);
            if ((car >= FIRST) && (car <= LAST)) {
                String newVal = _charMap.get(car - FIRST);
                buffer.replace(bcl, bcl + 1, newVal);
            }
        }
        return buffer.toString();
    }

    private static void initCharMap() {
        String car = null;

        car = "A"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00C0' � alt-0192 */
        _charMap.add(car); /* '\u00C1' � alt-0193 */
        _charMap.add(car); /* '\u00C2' � alt-0194 */
        _charMap.add(car); /* '\u00C3' � alt-0195 */
        _charMap.add(car); /* '\u00C4' � alt-0196 */
        _charMap.add(car); /* '\u00C5' � alt-0197 */
        car = "AE"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00C6' � alt-0198 */
        car = "C"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00C7' � alt-0199 */
        car = "E"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00C8' � alt-0200 */
        _charMap.add(car); /* '\u00C9' � alt-0201 */
        _charMap.add(car); /* '\u00CA' � alt-0202 */
        _charMap.add(car); /* '\u00CB' � alt-0203 */
        car = "I"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00CC' � alt-0204 */
        _charMap.add(car); /* '\u00CD' � alt-0205 */
        _charMap.add(car); /* '\u00CE' � alt-0206 */
        _charMap.add(car); /* '\u00CF' � alt-0207 */
        car = "D"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00D0' � alt-0208 */
        car = "N"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00D1' � alt-0209 */
        car = "O"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00D2' � alt-0210 */
        _charMap.add(car); /* '\u00D3' � alt-0211 */
        _charMap.add(car); /* '\u00D4' � alt-0212 */
        _charMap.add(car); /* '\u00D5' � alt-0213 */
        _charMap.add(car); /* '\u00D6' � alt-0214 */
        car = "*"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00D7' � alt-0215 */
        car = "0"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00D8' � alt-0216 */
        car = "U"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00D9' � alt-0217 */
        _charMap.add(car); /* '\u00DA' � alt-0218 */
        _charMap.add(car); /* '\u00DB' � alt-0219 */
        _charMap.add(car); /* '\u00DC' � alt-0220 */
        car = "Y"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00DD' � alt-0221 */
        _charMap.add(car); /* '\u00DE' � alt-0222 */
        car = "B"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00DF' � alt-0223 */
        car = "a"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00E0' � alt-0224 */
        _charMap.add(car); /* '\u00E1' � alt-0225 */
        _charMap.add(car); /* '\u00E2' � alt-0226 */
        _charMap.add(car); /* '\u00E3' � alt-0227 */
        _charMap.add(car); /* '\u00E4' � alt-0228 */
        _charMap.add(car); /* '\u00E5' � alt-0229 */
        car = "ae"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00E6' � alt-0230 */
        car = "c"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00E7' � alt-0231 */
        car = "e"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00E8' � alt-0232 */
        _charMap.add(car); /* '\u00E9' � alt-0233 */
        _charMap.add(car); /* '\u00EA' � alt-0234 */
        _charMap.add(car); /* '\u00EB' � alt-0235 */
        car = "i"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00EC' � alt-0236 */
        _charMap.add(car); /* '\u00ED' � alt-0237 */
        _charMap.add(car); /* '\u00EE' � alt-0238 */
        _charMap.add(car); /* '\u00EF' � alt-0239 */
        car = "d"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00F0' � alt-0240 */
        car = "n"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00F1' � alt-0241 */
        car = "o"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00F2' � alt-0242 */
        _charMap.add(car); /* '\u00F3' � alt-0243 */
        _charMap.add(car); /* '\u00F4' � alt-0244 */
        _charMap.add(car); /* '\u00F5' � alt-0245 */
        _charMap.add(car); /* '\u00F6' � alt-0246 */
        car = "/"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00F7' � alt-0247 */
        car = "0"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00F8' � alt-0248 */
        car = "u"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00F9' � alt-0249 */
        _charMap.add(car); /* '\u00FA' � alt-0250 */
        _charMap.add(car); /* '\u00FB' � alt-0251 */
        _charMap.add(car); /* '\u00FC' � alt-0252 */
        car = "y"; //$NON-NLS-1$
        _charMap.add(car); /* '\u00FD' � alt-0253 */
        _charMap.add(car); /* '\u00FE' � alt-0254 */
        _charMap.add(car); /* '\u00FF' � alt-0255 */
        _charMap.add(car); /* '\u00FF' alt-0255 */
    }

    public static String encode(String str, String charset) {
        try {

            // checks if encode is needed
            CharBuffer buffer = CharBuffer.wrap(str.toCharArray());
            for (char c : str.toCharArray()) {
                if (c == 'à')
                    return str;
                if (c == 'â')
                    return str;
                if (c == 'é')
                    return str;
                if (c == 'è')
                    return str;
                if (c == 'ê')
                    return str;
                if (c == 'î')
                    return str;
                if (c == 'ô')
                    return str;
                if (c == 'ù')
                    return str;
                if (c == 'û')
                    return str;
                if (c == '°')
                    return str;
            }

            // do encoding
            if (!Charset.isSupported(charset))
                return str;
            CharsetEncoder encoder = Charset.forName(charset).newEncoder();
            if (encoder.canEncode(str)) {
                return new String(encoder.encode(buffer).array());
            }
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }
        return str;
    }

}