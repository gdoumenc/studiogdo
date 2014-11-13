/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.key;

import java.util.Arrays;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.slot.MultiSlot;
import com.gdo.stencils.util.SlotUtils;

/**
 * <p>
 * Unique string key for plugging stencil is a slot.
 * </p>
 * <p>
 * When a stencil is plugged in a {@link com.gdo.stencils.slot.MultiSlot} with a
 * key, this key must be unique. To be sure that this key is unique, if it is a
 * string, this key can be encapsulated in a <tt>UniqueKey</tt> object, which
 * will give another possible values if another stencil is already plugged into
 * the slot.
 * </p>
 * <p>
 * A unique key is a key which provides another values if the key is already
 * defined in a slot the other values are created by <tt>getNextKey()</tt>
 * method. <tt>generateNextKey()</tt> method in the subclass implements the
 * unique key generation. </ul>
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com)</a>
 */
public abstract class _KeyGenerator<C extends _StencilContext, S extends _PStencil<C, S>, K extends Comparable<K>> implements IKeyGenerator {

    protected Key _key; // actual key used for plugging

    /**
     * From a first key proposal, create a new key generator from a slot.
     * 
     * @param key
     *            first key estimated.
     */
    public _KeyGenerator(C stclContext, Key key, PSlot<C, S> slot) {
        _key = key;

        // the value may change on multi slot only
        if (SlotUtils.isMultiple(stclContext, slot)) {
            MultiSlot<C, S> multi = (MultiSlot<C, S>) slot.getSlot();

            // key generator are not available on cursor slot
            if (!multi.isCursorBased(stclContext)) {

                // search next unique key value in slot
                IKey[] keys = multi.getKeys(stclContext, slot);
                Arrays.sort(keys);

                // while the current key exists in slot, generate another one
                while (Arrays.binarySearch(keys, getKey()) >= 0) {
                    generateNextKey();
                }
            }
        }
    }

    /**
     * Returns the key defined which may has evolved.
     * 
     * @return the key defined.
     */
    @Override
    public final IKey getKey() {
        return _key;
    }

    /**
     * Must be defined to retrieve a new key from the one existing.
     */
    abstract protected void generateNextKey();
}