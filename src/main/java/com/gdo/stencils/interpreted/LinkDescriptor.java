/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.interpreted;

import java.io.IOException;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Link descriptor class.
 * <p>

 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>

 */
public final class LinkDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends _Descriptor<C, S> {

    private String _slot; // slot name where the link will be plugged
    private String _key; // plug key for the link plugged
    private String _path; // path from reference (for substencil plug)
    private String _local; // local link

    public String getSlot() {
        return _slot;
    }

    // used by digester
    public void setSlot(String slot) {
        _slot = slot;
    }

    public String getKey() {
        return _key;
    }

    // used by digester
    public void setKey(String key) {
        _key = key;
    }

    public String getPath() {
        return _path;
    }

    // used by digester
    public void setPath(String path) {
        _path = path;
    }

    public String getLocal() {
        return _local;
    }

    // used by digester
    public void setLocal(String local) {
        _local = local;
    }

    @Override
    public void save(C stclContext, XmlWriter instOut, XmlWriter plugOut) throws IOException {
    }
}