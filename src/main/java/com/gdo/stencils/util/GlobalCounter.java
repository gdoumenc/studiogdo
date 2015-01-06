package com.gdo.stencils.util;

/**
 * <p>
 * The <tt>GlobalCounter</tt> class is a global counter implementation.
 * </p>
 * <p>
 * Main classes defined in this project are subclasses of this atom class.
 * </p>
 */
public abstract class GlobalCounter {

    // unique identifier for a null atom
    public static final String NULL_ATOM_ID = "null";

    // global session atom counter
    private static int COUNTER = 0;

    /**
     * Gets a session unique identifier.
     * 
     * @return the session unique identifier.
     */
    public static int uniqueInt() {
        if (COUNTER == Integer.MAX_VALUE) {
            COUNTER = 0;
        }
        return COUNTER++;
    }

    public static String ID() {
        return Integer.toString(uniqueInt());
    }

    public static String uniqueID() {
        StringBuffer str = new StringBuffer();
        str.append('_').append(uniqueInt()).append(time());
        return str.toString();
    }

    // return time stamp
    private static String time() {
        return Long.toString(System.currentTimeMillis());
    }

}