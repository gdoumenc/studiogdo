package com.gdo.project.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.gdo.project.model.SessionStcl.Slot;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiSlot;

public class SlotLimitSlot extends MultiSlot<StclContext, PStcl> {

    // keys defined in the slot
    private List<String> _keys = new ArrayList<String>();

    public SlotLimitSlot(StclContext stclContext, Stcl in) {
        super(stclContext, in, Slot.SLOT_LIMIT);
        _verify_unique = false; // do not remove to avoid recursion on creation
    }

    @Override
    protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

        // checks the property exists for key if needed
        String key = PathCondition.<StclContext, PStcl> getKeyCondition(cond);
        if (StringUtils.isNotBlank(key)) {

            // creates property if not exist
            if (!_keys.contains(key)) {
                PStcl container = self.getContainer();
                container.newPProperty(stclContext, Slot.SLOT_LIMIT, new Key<String>(key), "");
                _keys.add(key);
            }
        }

        // return usual multi slot content
        return super.getStencilsList(stclContext, cond, self);
    }
}
