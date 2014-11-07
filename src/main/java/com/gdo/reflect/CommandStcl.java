/**
 * Copyright GDO - 2005
 */
package com.gdo.reflect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import com.gdo.helper.IOHelper;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.prop.IPropCalculator;
import com.gdo.stencils.slot.SingleCalculatedPropertySlot;

public class CommandStcl extends Stcl {

    public interface Slot extends Stcl.Slot {
        String COMMAND = "Command";
    }

    public CommandStcl(StclContext stclContext) {
        super(stclContext);

        singleSlot(Slot.COMMAND);

        // change $TemplateName calculator to give contained property template
        // name
        PSlot<StclContext, PStcl> name = self(stclContext, null).getSlot(stclContext, Slot.$TEMPLATE_NAME);
        SingleCalculatedPropertySlot<StclContext, PStcl> n = (SingleCalculatedPropertySlot<StclContext, PStcl>) name.getSlot();
        n.setCalculator(new TemplateNameCalculator());
    }

    class TemplateNameCalculator implements IPropCalculator<StclContext, PStcl> {
        public Class<String> getValueClass() {
            return String.class;
        }

        @Override
        public String getValue(StclContext stclContext, PStcl self) {
            return self.getString(stclContext, Slot.$TEMPLATE_NAME, null);
        }

        @Override
        public String setValue(StclContext stclContext, String value, PStcl self) {
            String msg = String.format("Cannot change %s value", Slot.$TEMPLATE_NAME);
            throw new NotImplementedException(msg);
        }

        @Override
        public InputStream getInputStream(StclContext stclContext, PStcl self) {
            try {
                String value = getValue(stclContext, self);
                if (value != null) {
                    return IOUtils.toInputStream(value, StclContext.getCharacterEncoding());
                }
            } catch (IOException e) {
            }
            return IOHelper.EMPTY_INPUT_STREAM;
        }

        @Override
        public OutputStream getOutputStream(StclContext stclContext, PStcl self) {
            return IOHelper.EMPTY_OUTPUT_STREAM;
        }
    }
}
