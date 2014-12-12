package com.gdo.stencils.atom;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * The <tt>Atom</tt> class is the simpliest abstract class that implements the
 * {@link IAtom} interface.
 * </p>
 * <p>
 * Main classes defined in this project are subclasses of this atom class.
 * </p>
 */
public abstract class Atom implements IAtom<_PStencil> {

    // unique identifier for a null atom
    public static final String NULL_ATOM_ID = "null";

    // global session atom counter
    private static int COUNTER = 0;

    // internal identifier in session
    private String _id = null;

    // unique identifier for stored component
    private String _uid = null;

    /**
     * @return this object casted to the generic signature.
     */
    @Override
    public _PStencil self() {
        return (_PStencil) this;
    }

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

    /**
     * Gets an universal unique identifier
     * 
     * @return the universal unique identifier defined from session counter and
     *         time.
     */
    public static String uniqueID() {
        StringBuffer str = new StringBuffer();
        str.append('_').append(uniqueInt()).append(time());
        return str.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.stencils.atom.IAtom#getId(java.lang.Object)
     */
    @Override
    public String getId(_StencilContext stclContext) {
        if (_id == null) {
            StringBuffer str = new StringBuffer();
            str.append('_').append(uniqueInt());
            _id = str.toString();
        }
        return _id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gdo.stencils.atom.IAtom#getUId(java.lang.Object)
     */
    @Override
    public String getUId(_StencilContext stclContext) {
        if (_uid == null) {
            _uid = getId(stclContext) + time();
        }
        return _uid;
    }

    // return time stamp
    private static String time() {
        return Long.toString(System.currentTimeMillis());
    }

}