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

        @Override
        public int toInt() {
            return 0;
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
        return _key.toString();
    }

    public K getValue() {
        return _key;
    }

    @SuppressWarnings("unchecked")
    @Deprecated
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
        if (o == null)
            return 0;
        return _key.toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(Object key) {
        if (key == null)
            return false;
        return _key.toString().equals(key.toString());
    }

    @Override
    public boolean isEmpty() {
        if (_key instanceof String) {
            return StringUtils.isBlank((String) _key);
        }
        return false;
    }

    @Override
    public boolean isNotEmpty() {
        if (_key instanceof String) {
            return StringUtils.isNotBlank((String) _key);
        }
        return true;
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
