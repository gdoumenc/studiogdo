package com.gdo.stencils.key;


/**
 * <p>
 * The <tt>IKey</tt> interface is used to manipulate key for plugged stencil instances.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 */
public interface IKey extends Comparable<IKey> {

    /**
     * @return <tt>true</tt> if the key is null
     */
    boolean isEmpty();

    /**
     * @return <tt>true</tt> if the key is not null
     */
    boolean isNotEmpty();

    default int toInt() {
        if (isEmpty()) {
            return 0;
        }
        return Integer.parseInt(toString());
    }

}
