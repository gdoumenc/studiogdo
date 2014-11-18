package com.gdo.project.slot;

import com.gdo.stencils.StclContext;
import com.gdo.stencils._Stencil;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;

/**
 * <p>
 * This class allows to create a slot with a default modifiable string property
 * in it.
 * </p>
 */
public class StringProxySlot extends CalculatedStringPropertySlot<StclContext, PStcl> {

    public interface Proxy {
        String getValue();

        void setValue(String value);
    }

    private Proxy _proxy;

    public StringProxySlot(StclContext stclContext, _Stencil<StclContext, PStcl> in, String name, Proxy proxy) {
        super(stclContext, in, name);
        _proxy = proxy;
    }

    public StringProxySlot(StclContext stclContext, _Stencil<StclContext, PStcl> in, String name, String value) {
        super(stclContext, in, name);
        _proxy = new StringProxy(value);
    }

    @Override
    public String getValue(StclContext stclContext, PStcl self) {
        return _proxy.getValue();
    }

    @Override
    public String setValue(StclContext stclContext, String value, PStcl self) {
        _proxy.setValue(value);
        return null; // TODO to change return value ofsetValue from Proxy
    }

    private class StringProxy implements Proxy {
        private String _value;

        public StringProxy(String value) {
            _value = value;
        }

        @Override
        public String getValue() {
            return _value;
        }

        @Override
        public void setValue(String value) {
            throw new IllegalStateException("proxy to a read only string");
        }
    }
}
