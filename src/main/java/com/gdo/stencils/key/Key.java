package com.gdo.stencils.key;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.log.StencilLog;

public class Key<K> implements IKey, Comparable<IKey> {

    // const NO_KEY
    public static final IKey NO_KEY = new Key<String>(StringHelper.EMPTY_STRING) {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean isNotEmpty() {
            return false;
        }

        @Override
        public String toString() {
            return "";
        }
    };

    private K _key; // the real key

    public Key(K value) {
        if (value == null) {
            String msg = logError(null, "creation of a key with null value");
            throw new NullPointerException(msg);
        }
        _key = value;
    }

    @Override
    public String toString() {
        if (_key == null)
            return "";
        return _key.toString();
    }

    public K getValue() {
        return _key;
    }

    @SuppressWarnings("unchecked")
    public void changeTo(String value) {
        if (_key instanceof String) {
            _key = (K) value;
        } else if (_key instanceof Integer) {
            _key = (K) new Integer(value);
        }
    }

    @Override
    public int hashCode() {
        return _key.hashCode();
    }

    @Override
    public int compareTo(IKey o) {
        if (o == null || _key == null)
            return 0;
        return _key.toString().compareTo(o.toString());
    }

    @Override
    public boolean isEmpty() {
        return _key == null || StringUtils.isEmpty(_key.toString());
    }

    @Override
    public boolean isNotEmpty() {
        return _key != null && StringUtils.isNotEmpty(_key.toString());
    }

    @Override
    public boolean equals(Object key) {
        if (_key == null || key == null)
            return false;
        return _key.toString().equals(key.toString());
    }

    //
    // LOG PART
    //

    protected StencilLog getLog() {
        return _Stencil._LOG;
    }

    public String logTrace(_StencilContext stclContext, String format, Object... params) {
        return getLog().logTrace(stclContext, format, params);
    }

    public String logWarn(_StencilContext stclContext, String format, Object... params) {
        return getLog().logWarn(stclContext, format, params);
    }

    public String logError(_StencilContext stclContext, String format, Object... params) {
        return getLog().logError(stclContext, format, params);
    }
}
