/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.key;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;

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
 * defined in a slot the other values are created by concatening this string
 * with "1".
 * </p>

 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>

 */
public class StringKeyGenerator<C extends _StencilContext, S extends _PStencil<C, S>> extends _KeyGenerator<C, S, String> {

    public StringKeyGenerator(C stclContext, String key, PSlot<C, S> slot) {
        super(stclContext, new Key(key), slot);
    }

    @Override
    protected void generateNextKey() {
        _key.changeTo(_key.getValue() + "1");
    }

}