/*
 * Copyright GDO - 2004
 */
package com.gdo.stencils.cmd;

import java.util.Map;
import java.util.Properties;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * A command context is used as a dynamic parameters container for command
 * execution.
 * </p>
 * <p>
 * An execution context stores all informations used by a command for its
 * execution.
 * </p>
 * 
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 */
@SuppressWarnings("serial")
public class CommandContext<C extends _StencilContext, S extends _PStencil<C, S>> extends Properties {

    public static final String PARAM(int index) {
        return String.format("param%s", index);
    }

    public static final String[] PARAMS = new String[] { PARAM(1), PARAM(2), PARAM(3), PARAM(4), PARAM(5), PARAM(6), PARAM(7), PARAM(8), PARAM(9), PARAM(10), PARAM(11), PARAM(12), PARAM(13),
            PARAM(14), PARAM(15), PARAM(16), PARAM(17), PARAM(18), PARAM(19), PARAM(20) };

    private C _stclContext; // stencil context
    private S _target; // the target on which the command is performed
    private Map<String, Object> _defParams; // default parameters in delegation

    // call

    public CommandContext(C stclContext, S target) {
        _stclContext = stclContext;
        _target = target;
    }

    public final S getTarget() {
        return _target;
    }

    public final void setTarget(S target) {
        _target = target;
    }

    /**
     * Returns the stencil context.
     * 
     * @return the stencil context.
     */
    public final C getStencilContext() {
        return _stclContext;
    }

    public void setDelegationDefaultParameters(Map<String, Object> def) {
        _defParams = def;
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        if (super.containsKey(key))
            return true;
        if (defaultContainsKey(key))
            return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    public final <K> K getRedefinedParameter(String key) {
        if (super.containsKey(key))
            return (K) get(key);
        if (defaultContainsKey(key))
            return (K) _defParams.get(key);
        return null;
    }

    public final void setRedefinedParameter(String key, Object value) {
        if (value != null)
            put(key, value);
        else
            remove(key);
    }

    public boolean defaultContainsKey(Object key) {
        return (_defParams != null && _defParams.containsKey(key));
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized CommandContext<C, S> clone() {
        CommandContext<C, S> clone = (CommandContext<C, S>) super.clone();
        clone._stclContext = getStencilContext();
        clone._target = getTarget();
        return clone;
    }

}