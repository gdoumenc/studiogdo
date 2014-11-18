/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.interpreted;

import java.io.IOException;
import java.io.StringWriter;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.factory.IStencilFactory.Mode;
import com.gdo.stencils.plug._PStencil;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Command descriptor class.
 * </p>

 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>

 */
public final class CommandDescriptor<C extends _StencilContext, S extends _PStencil<C, S>> extends InstDescriptor<C, S> {

    @Override
    public void addInstDescriptor(InstDescriptor<C, S> inst) {
        super.addInstDescriptor(inst);
        inst.setOn(Mode.ON_ALWAYS);
        for (PlugDescriptor<C, S> plug : inst.getPlugDescriptors()) {
            plug.setOnAsMode(Mode.ON_ALWAYS);
        }
    }

    /**
     * A plug in a command is always performed as the command is always
     * recreated.
     */
    @Override
    public void addPlugDescriptor(PlugDescriptor<C, S> plug) {
        super.addPlugDescriptor(plug);
        plug.setOnAsMode(Mode.ON_ALWAYS); // as a command is never stored
        // (should be set after super called)
    }

    @Override
    public void save(C stclContext, XmlWriter declPart, XmlWriter plugPart) throws IOException {

        // checks parameters
        if (plugPart != null) {
            throw new IllegalArgumentException("plug xml writer should be null for instance descriptor");
        }

        declPart.startElement("command");
        declPart.writeAttribute("name", getName());
        declPart.writeAttribute("template", getTemplate());
        saveParameters(stclContext, declPart);
        StringWriter body = new StringWriter();
        saveDescription(stclContext, declPart, new XmlWriter(body, 0));
        declPart.write(body.getBuffer().toString());
        declPart.endElement("command");
    }

}