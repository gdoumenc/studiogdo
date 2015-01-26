/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.factory;

import com.gdo.helper.ClassHelper;
import com.gdo.stencils.CommandStcl;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.interpreted.TemplateDescriptor;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.prop.CalculatedPropStcl;

/**
 * <p>
 * This stencil factory defines default classes used in studiogdo.
 * <p>
 * 
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * 
 * 
 * @author Guillaume Doumenc
 */
public class StclFactory extends InterpretedStencilFactory<StclContext, PStcl> {

    // default classes
    private static final Class<PStcl> DEFAULT_PSTENCIL_CLASS = PStcl.class;
    private static final String STENCIL_DEFAULT_TEMPLATE_NAME = Stcl.class.getName();
    private static final String PROPERTY_DEFAULT_TEMPLATE_NAME = Stcl.class.getName();
    private static final String CALCULATED_PROPERTY_DEFAULT_TEMPLATE_NAME = CalculatedPropStcl.class.getName();

    @Override
    public Class<PStcl> getDefaultPStencilClass(StclContext stclContext) {
        return DEFAULT_PSTENCIL_CLASS;
    }

    @Override
    public String getStencilDefaultTemplateName(StclContext stclContext) {
        return STENCIL_DEFAULT_TEMPLATE_NAME;
    }

    @Override
    public String getPropertyDefaultTemplateName(StclContext stclContext) {
        return PROPERTY_DEFAULT_TEMPLATE_NAME;
    }

    @Override
    public String getCalculatedPropertyDefaultTemplateName(StclContext stclContext) {
        return CALCULATED_PROPERTY_DEFAULT_TEMPLATE_NAME;
    }

    public PStcl cloneStencil(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key, PStcl self) {

        // gets template descriptor
        Stcl stcl = self.getReleasedStencil(stclContext);
        TemplateDescriptor<StclContext, PStcl> tempDesc = stcl.getDescriptor();

        // creates stencil from descriptor
        Stcl created;
        if (tempDesc != null) {
            created = createStencil(stclContext, tempDesc);
        } else {
            created = super.createStencil(stclContext, stcl.getClass());
        }

        // returns plugged stencil
        Class<PStcl> pstencilClass = getDefaultPStencilClass(stclContext);
        return ClassHelper.newInstance(pstencilClass, stclContext, created, slot, key);
    }

    public PStcl cloneCommand(StclContext stclContext, PSlot<StclContext, PStcl> slot, IKey key, PStcl self) {

        // gets template descriptor
        CommandStcl stcl = self.getReleasedStencil(stclContext);
        TemplateDescriptor<StclContext, PStcl> tempDesc = stcl.getDescriptor();

        // creates stencil from descriptor
        CommandStcl created;
        if (tempDesc != null) {
            created = createStencil(stclContext, tempDesc);
        } else {
            created = super.createStencil(stclContext, stcl.getClass());
        }

        // returns plugged stencil
        Class<PStcl> pstencilClass = getDefaultPStencilClass(stclContext);
        return ClassHelper.newInstance(pstencilClass, stclContext, created, slot, key);
    }
}