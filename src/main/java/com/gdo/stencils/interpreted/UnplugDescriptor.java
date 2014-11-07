/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.interpreted;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils.Keywords;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.factory.IStencilFactory;
import com.gdo.stencils.factory.IStencilFactory.Mode;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Unplug descriptor class.
 * <p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com)</a>
 * @see com.gdo.stencils.cmd.CommandContext Context
 */
public final class UnplugDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends _Descriptor<C, S> {

    private String _slot; // slot name
    private String _key; // plug key
    private IStencilFactory.Mode _on = IStencilFactory.Mode.ON_LOAD; // when the

    // plug
    // should
    // be
    // performed
    // (by
    // default
    // on load)

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

    public IStencilFactory.Mode getOnAsMode() {
        return _on;
    }

    // used by digester
    public void setOn(String on) {
        if (Keywords.CREATE.equals(on)) {
            _on = Mode.ON_CREATION;
        } else if (Keywords.LOAD.equals(on)) {
            _on = Mode.ON_LOAD;
        } else if (Keywords.ALWAYS.equals(on)) {
            _on = Mode.ON_ALWAYS;
        } else {
            logWarn(null, "Unknow unplug on mode : " + on);
        }
    }

    // set the plug on slot to be used on lazy evaluation
    public void setOnSlot(C stclContext, S container, InstanceRepository<C, S> instances, int completionLevel) {

        // if the plug should be done only for creation, do nothing in loading
        // and vice-versa
        IStencilFactory.Mode mode = getOnAsMode();
        if (!mode.equals(Mode.ON_ALWAYS) && mode != instances.getMode()) {
            return;
        }

        // set the plug on the slot
        try {
            String slotName = getSlot();
            PSlot<C, S> slot = container.getSlot(stclContext, slotName);

            // unplug in slot only if plug is defined after the slot (no plug in
            // case of redefinition)
            if (completionLevel <= slot.getSlot().getCompletionLevel()) {
                String key = getKey();
                if (!StringUtils.isEmpty(key)) {
                    IKey k = new Key<String>(key);
                    slot.getSlot().unplug(stclContext, null, k, slot);
                } else {
                    slot.getSlot().unplugAll(stclContext, slot);
                }
            }
        } catch (Exception e) {
            logWarn(stclContext, "Exception when unplugging key %s in %s in slot %s ", getKey(), container, getSlot());
        }
    }

    @Override
    public void save(C stclContext, XmlWriter instOut, XmlWriter plugOut) throws IOException {

        // don't save creation plug (it is defined in template description)
        if (getOnAsMode() == IStencilFactory.Mode.ON_CREATION)
            return;

        // starts the plug tag
        plugOut.startElement("unplug");

        // saves slot and key
        plugOut.writeAttribute("slot", getSlot());
        String key = getKey();
        if (!StringUtils.isEmpty(key))
            plugOut.writeAttribute("key", key);

        // closes plug tag
        plugOut.endElement("unplug");
    }
}