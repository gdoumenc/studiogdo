/**
 * Copyright GDO - 2003
 */
package com.gdo.stencils.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gdo.helper.StringHelper;

/**
 * <p>
 * Path manipulation helper.
 * </p>
 * A composed path represents an element in a hierarchical description of
 * plugged elements. A composed path is made of '/' except the simple path "/"
 * which represents just the root project so is not composed.
 */
public abstract class PathUtils {
    protected static final Log log = LogFactory.getLog(PathUtils.class);

    // composition and expression
    public static final char SEP = '/'; // directory separator
    public static final String SEP_STR = Character.toString(SEP);
    public static final int SEP_INT = 1;
    public static final char KEY_SEP_OPEN = '(';
    public static final char KEY_SEP_CLOSE = ')';
    public static final char EXP_SEP_OPEN = '[';
    public static final char EXP_SEP_CLOSE = ']';

    public static final char MULTI = ':'; // for multiple pathes

    // specific pathes
    public static final String ROOT = "/";
    public static final String THIS = ".";
    public static final String PARENT = "..";
    public static final String NUMBER = "#";
    public static final String NUMBER_NUMBER = "##";
    public static final String KEY = "@";
    public static final String ABSOLUTE_PATH = "^";
    public static final String UID = "&";

    // expression operator
    public static final String COMMAND_OPER = "!";
    public static final String EQUALS_OPER = "==";
    public static final String DIFF_OPER = "!=";
    public static final String STARTS_WITH_OPER = "^=";
    public static final String ENDS_WITH_OPER = "=^";
    public static final String MATCHES_OPER = "~=";
    public static final String[] OPERS = { EQUALS_OPER, DIFF_OPER, STARTS_WITH_OPER, ENDS_WITH_OPER, MATCHES_OPER };

    private PathUtils() {
        // utility class, disable instanciation
    }

    /**
     * Cheks if a path is absolute (starts with '/').
     * 
     * @param path
     *            the path to check.
     * @return <tt>true</tt> if a path is absolute.
     */
    public static final boolean isAbsolute(String path) {
        if (StringUtils.isBlank(path)) {
            return false;
        }
        return path.charAt(0) == SEP;
    }

    /**
     * Cheks if a path is absolute (contains '/' but not in key or expression
     * part).
     * 
     * @param path
     *            the path to check.
     * @return <tt>true</tt> if a path is a composed path (contains '/' but not
     *         in key or expression part).
     */
    public static final boolean isComposed(String path) {

        // no path
        if (StringUtils.isBlank(path)) {
            return false;
        }

        // avoid root code ("/")
        if (path.length() == 1) {
            return false;
        }

        return (indexOf(path, SEP_STR) != -1);
    }

    /**
     * Returns the path or "." is path is blank.
     * 
     * @return the path or "." is path is blank.
     */
    public static final String getPathOrThis(String path) {
        if (StringUtils.isBlank(path)) {
            return PathUtils.THIS;
        }
        return StringUtils.trim(path);
    }

    /**
     * Returns the last name in a composed path.
     * <p>
     * This name is the last element in the hierarchy path, usually a slot
     * expression. See also getPathName.
     * </p>
     * 
     * @return the last name in the composed path or the path itself if the path
     *         is not composed.
     */
    public static final String getLastName(String path) {
        int index = lastIndexOf(path, SEP_STR);
        if (index >= 0) {
            return StringUtils.trim(path.substring(index + 1));
        }
        return path;
    }

    /**
     * Returns the first names in a composed path.
     * <p>
     * This name is the the hierarchy path of the container stancil.
     * </p>
     * 
     * @return the path name in the composed path or empty string if the path is
     *         not composed.
     */
    public static final String getPathName(String path) {
        if (StringUtils.isEmpty(path)) {
            return StringHelper.EMPTY_STRING;
        }

        int index = lastIndexOf(path, SEP_STR);
        if (index == 0) {
            return ROOT;
        }
        if (index > 0) {
            return StringUtils.trim(path.substring(0, index));
        }
        return StringHelper.EMPTY_STRING;
    }

    /**
     * Returns the first name in a composed path.
     * <p>
     * The first name in a composed path represents the first element in the
     * hierarchy path.
     * </p>
     * 
     * @return the first name is the composed path or empty string if the path
     *         is not composed.
     */
    public static final String getFirstName(String path) {
        if (StringUtils.isEmpty(path)) {
            return StringHelper.EMPTY_STRING;
        }

        int index = indexOf(path, SEP_STR);
        if (index == 0) {
            return ROOT;
        }
        if (index > 0) {
            return StringUtils.trim(path.substring(0, index));
        }
        return StringHelper.EMPTY_STRING;
    }

    /**
     * Returns the last names in a composed path.
     * <p>
     * The last names in a composed path represents the path of the element
     * described by the hierarchy path starting from the first element.
     * </p>
     * 
     * @return the first names is the composed path or the path itself if the
     *         path is not composed.
     */
    public static final String getTailName(String path) {
        if (StringUtils.isEmpty(path)) {
            return StringHelper.EMPTY_STRING;
        }

        int index = indexOf(path, SEP_STR);
        if (index >= 0 && index < path.length()) {
            return StringUtils.trim(path.substring(index + 1));
        }
        return path;
    }

    /**
     * Splits a composed path into an array of slot's names.
     * 
     * @param path
     *            the complete path (will be normalized before splitting).
     * @return an array of slots names.
     */
    public static final String[] split(String path) {
        return StringHelper.splitShortStringAndTrim(normalize(path), SEP);
    }

    /**
     * Splits a multi path into an array of single normalized pathes (multi path
     * are separated by ':').
     * 
     * @param path
     *            the complete path.
     * @return a pathes array.
     */
    public static final String[] splitMultiPath(String path) {
        String[] pathes = StringHelper.splitShortStringAndTrim(path, MULTI);
        for (int i = 0; i < pathes.length; i++) {
            pathes[i] = normalize(pathes[i]);
        }
        return pathes;
    }

    public static final String getCondition(String path) {

        // nothing to do if path is blank
        if (StringUtils.isBlank(path)) {
            return "";
        }

        // searches only in last slot name
        String last = getLastName(path);

        int start1 = indexOf(last, Character.toString(KEY_SEP_OPEN));
        int start2 = indexOf(last, Character.toString(EXP_SEP_OPEN));
        int start = (start1 >= start2) ? start1 : start2;
        return last.substring(start);
    }

    /**
     * Tests if a name describes a contained stencil identified by a key. Such a
     * name has the form : container(key)
     * <p>
     * A multi contained stencil is determined uniquely by its key in the list
     * of plugged stencils.
     * </p>
     * 
     * @see #getKeyContainer(String)
     * @see #getKeyContained(String)
     * @param path
     *            stencil path.
     * @return <tt>true</tt> if the name represents a named contained stencil.
     */
    public static final boolean isKeyContained(String path) {

        // nothing to do if path is blank
        if (StringUtils.isBlank(path)) {
            return false;
        }

        // searches only in last slot name
        String last = getLastName(path);

        // no key if no '('
        int start = indexOf(last, Character.toString(KEY_SEP_OPEN));
        if (start == -1) {
            return false;
        }

        // verifies well formed (key must be at least 1 char long)
        int stop = lastIndexOf(last, Character.toString(KEY_SEP_CLOSE));
        return (stop > start + 1);
    }

    /**
     * Returns the container's name of a multi contained stencil.
     * 
     * @param path
     *            stencil path.
     * @return the container's name.
     * @see #getKeyContained(String)
     */
    public static final String getKeyContainer(String path) {

        // nothing to do if path is blank
        if (StringUtils.isBlank(path)) {
            return StringHelper.EMPTY_STRING;
        }

        // searches last '('
        int start = lastIndexOf(path, Character.toString(KEY_SEP_OPEN));
        if (start != -1) {
            return path.substring(0, start);
        }

        // not found
        return path;
    }

    /**
     * Returns the unique key identifier of a multi contained stencil.
     * 
     * @param path
     *            stencil path.
     * @return the key identifying the stencil in the list of contained stencil.
     * @see #getKeyContainer(String)
     */
    public static final String getKeyContained(String path) {

        // nothing to do if path is blank
        if (StringUtils.isEmpty(path)) {
            return StringHelper.EMPTY_STRING;
        }

        // searches only in last slot name
        String last = getLastName(path);

        // gets substring
        int start = indexOf(last, Character.toString(KEY_SEP_OPEN));
        int stop = indexOf(last, Character.toString(KEY_SEP_CLOSE));
        if (start != -1 && stop != -1 && stop > start) {
            return last.substring(start + 1, stop);
        }

        // not found should not be called
        return StringHelper.EMPTY_STRING;
    }

    /**
     * Tests if this name describes multi contained stencils. Such a name has
     * the form : <tt>container[exp (, exp)*]</tt> where <tt>exp</tt> is in the
     * form <tt>prop op value</tt> (<tt>op</tt> may be <tt>==</tt> or
     * <tt>!=</tt>).
     * 
     * @param path
     *            a simple stencil name, not a path with '/'.
     * @return true if the name represents a multi contained stencil.
     * @see #getExpContainer(String)
     * @see #getPropContained(String)
     */
    public static final boolean isExpContained(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }

        int start = indexOf(path, Character.toString(EXP_SEP_OPEN));
        if (start == -1) {
            return false;
        }
        int stop = lastIndexOf(path, Character.toString(EXP_SEP_CLOSE));
        return (start >= 0 && stop > start);
    }

    /**
     * Returns the container's name of a contained stencil.
     * 
     * @param path
     *            a simple stencil name, not a path with '/'.
     * @return the container's name.
     * @see #getPropContained(String)
     */
    public static final String getExpContainer(String path) {

        // nothing to do if path is blank
        if (StringUtils.isEmpty(path)) {
            return "";
        }

        // searches first '['
        int start = indexOf(path, Character.toString(EXP_SEP_OPEN));
        if (start != -1) {
            return path.substring(0, start);
        }

        // not found
        return path;
    }

    /**
     * Returns the expression defined in the name.
     * 
     * @param path
     *            a simple stencil name, not a path with '/'.
     * @return the expressions array defined in the name.
     */
    public static final String getExpContained(String path) {
        if (StringUtils.isEmpty(path)) {
            return StringHelper.EMPTY_STRING;
        }

        int start = indexOf(path, Character.toString(EXP_SEP_OPEN));
        if (start != -1) {
            return StringHelper.substringEnd(path.substring(start + 1), 1);
        }
        return null;
    }

    /**
     * Returns the stencil's property which is used to determine a contained
     * stencil.
     * <p>
     * If the property is not defined, then the default property is "Name".
     * 
     * @param path
     *            a simple stencil name, not a path with '/'.
     * @return the stencil's property identifying the stencil in the list of
     *         contained stencil. Null if the expression is wrongly structured.
     * @see #getExpContainer(String)
     * @see #getValueContained(String)
     */
    public static final String getPropContained(String path) {
        if (path == null || path.length() < 2) {
            return StringHelper.EMPTY_STRING;
        }

        // searches expression enclosed
        int start = indexOf(path, Character.toString(EXP_SEP_OPEN));
        int stop = lastIndexOf(path, Character.toString(EXP_SEP_CLOSE));
        if (start != -1 && stop != -1 && stop > start) {
            String exp = path.substring(start + 1, stop);

            // searches for any operator
            for (String oper : OPERS) {
                int index = exp.indexOf(oper);
                if (index != -1) {
                    return exp.substring(0, index).trim();
                }
            }

            // if no operator found, then test Name property (TODO should be
            // defulat property)
            return "Name";
        }
        return StringHelper.EMPTY_STRING;
    }

    /**
     * Returns the value that the stencil's property must be equal to determine
     * a contained stencil.
     * 
     * @param path
     *            a simple stencil name, not a path with '/'.
     * @return the stencil's value identifying the stencil in the list of
     *         contained stencil. Null if the expression is wrongly structured.
     * @see #getExpContainer(String)
     * @see #getPropContained(String)
     */
    public static final String getValueContained(String path) {
        if (StringUtils.isEmpty(path)) {
            return StringHelper.EMPTY_STRING;
        }

        int start = indexOf(path, Character.toString(EXP_SEP_OPEN));
        int stop = lastIndexOf(path, Character.toString(EXP_SEP_CLOSE));
        if (start != -1 && stop != -1) {
            String exp = path.substring(start + 1, stop);
            for (String oper : OPERS) {
                int index = exp.indexOf(oper);
                if (index != -1)
                    return exp.substring(index + 2);
            }
            return exp; // no operator found
        }
        return null;
    }

    public static final String getOperContained(String path) {
        if (StringUtils.isEmpty(path)) {
            return StringHelper.EMPTY_STRING;
        }

        int start = indexOf(path, Character.toString(EXP_SEP_OPEN));
        int stop = indexOf(path, Character.toString(EXP_SEP_CLOSE));
        if (start != -1 && stop != -1) {
            String exp = path.substring(start + 1, stop);
            for (String oper : OPERS) {
                int index = exp.indexOf(oper);
                if (index != -1)
                    return exp.substring(index, index + 2);
            }
            return "=="; // no operator found
        }
        return null;
    }

    /**
     * Create a path from a slot path and a key.
     * 
     * @param slotPath
     *            the slot's path.
     * @param key
     *            the key associated to the plug.
     * @return the stencil path.
     */
    // TODO rename by create
    public static final String createPath(String slotPath, Object key) {
        StringBuffer composed = new StringBuffer(slotPath);
        composed = composed.append(KEY_SEP_OPEN).append(key.toString()).append(KEY_SEP_CLOSE);
        return composed.toString();
    }

    /**
     * Create a path to a multi contained stencil from a slot path, a property
     * stencil and the value searched.
     * 
     * @param slotPath
     *            the slot's path.
     * @param prop
     *            the property used to determine the stencil.
     * @param value
     *            the value searched to determine the stencil.
     * @return the stencil path.
     */
    public static final String createPath(String slotPath, String prop, Object value) {
        return createPath(slotPath, prop, "==", value);
    }

    public static final String createPath(String slotPath, String prop, String oper, Object value) {
        StringBuffer str = new StringBuffer(slotPath);
        str.append(EXP_SEP_OPEN).append(prop).append(oper).append(value).append(EXP_SEP_CLOSE);
        return str.toString();
    }

    /**
     * Returns the slot name from a path (removes key and expresion if defined)
     * 
     * @param path
     *            the path where the slot name shoudl be extracted.
     * @return the slot name.
     */
    public static final String getSlotPath(String path) {
        if (path.endsWith(")")) {
            return getKeyContainer(path);
        }
        if (path.endsWith("]")) {
            return getExpContainer(path);
        }
        return path;
    }

    public static final String getKeyFromSlotPath(String path) {
        if (isKeyContained(path)) {
            return getKeyContained(path);
        }
        return StringHelper.EMPTY_STRING;
    }

    /**
     * Creates a composed path from a sequence of pathes.
     * 
     * @param pathes
     *            pathes to compose (with '/' between if needed)
     * @return the composed path.
     */
    public static final String compose(String... pathes) {

        // if no path then root
        if (pathes.length == 0) {
            return ROOT;
        }
        StringBuffer composed = new StringBuffer();

        // composes each path
        for (String path : pathes) {
            if (StringUtils.isNotEmpty(path)) {
                if (composed.length() == 0)
                    composed.append(path);
                else if (path.startsWith(ROOT)) {
                    composed = new StringBuffer(path);
                } else {
                    if (path.startsWith("./"))
                        path = path.substring(2);
                    if (composed.charAt(composed.length() - 1) == SEP)
                        composed.append(path);
                    else
                        composed.append(SEP).append(path);
                }
            }
        }

        return composed.toString().trim();
    }

    /**
     * Normalizes the path (removes '/' at end, '/./' patterns, ...).
     * 
     * @param path
     *            the path to be normalized.
     * @return the normalized path.
     */
    public static final String normalize(String path) {
        //
        // will use JDK1.7 nio Path.normalize after
        //

        String normalized = path;

        // removes last '/' if exists
        if (normalized.length() > 1 && normalized.endsWith(PathUtils.SEP_STR)) {
            normalized = StringHelper.substringEnd(normalized, 1);
        }

        return normalized;
    }

    /**
     * Returns the logger for this class.
     * 
     * @return the logger for this class.
     */
    public static final Log getLog() {
        return log;
    }

    /**
     * Searches for a character in the path but not in key or expression part.
     * 
     * @param path
     *            the path in which the character is searched.
     * @param ch
     *            the character serarched.
     * @return the position.
     */
    public static final int indexOf(String path, String ch) {
        int found = (ch.charAt(0) == KEY_SEP_CLOSE || ch.charAt(0) == EXP_SEP_CLOSE) ? 1 : 0;

        // checks path is not blank
        if (StringUtils.isBlank(path) || StringUtils.isBlank(ch)) {
            return -1;
        }

        int pos = 0;
        int inKeyPart = 0;
        int len = path.length();

        while (pos < len) {
            char c = path.charAt(pos);

            if (inKeyPart == found && path.substring(pos).startsWith(ch)) {
                return pos;
            }

            // deals with key part
            if (c == KEY_SEP_OPEN || c == EXP_SEP_OPEN) {
                inKeyPart++;
            } else if (c == KEY_SEP_CLOSE || c == EXP_SEP_CLOSE) {
                inKeyPart--;
            }

            pos++;
        }

        // not found
        return -1;
    }

    /**
     * Searches for a character in the path but not in key or expression part.
     * 
     * @param path
     *            the path in which the character is searched.
     * @param ch
     *            the character serarched.
     * @return the position.
     */
    public static final int lastIndexOf(String path, String ch) {
        int found = (ch.charAt(0) == KEY_SEP_CLOSE || ch.charAt(0) == EXP_SEP_CLOSE) ? 1 : 0;

        // checks path is not blank
        if (StringUtils.isBlank(path) || StringUtils.isBlank(ch)) {
            return -1;
        }

        int pos = 0;
        int last = -1;
        int inKeyPart = 0;
        int len = path.length();

        while (pos < len) {
            char c = path.charAt(pos);

            if (inKeyPart == found && path.substring(pos).startsWith(ch)) {
                last = pos;
            }

            // deals with key part
            if (c == KEY_SEP_OPEN || c == EXP_SEP_OPEN) {
                inKeyPart++;
            } else if (c == KEY_SEP_CLOSE || c == EXP_SEP_CLOSE) {
                inKeyPart--;
            }

            pos++;
        }

        // last found (perhas not)
        return last;
    }

}