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
        this._key = value;
    }

    @Override
    public String toString() {
        if (this._key == null)
            return "";
        return this._key.toString();
    }

    public K getValue() {
        return this._key;
    }

    @SuppressWarnings("unchecked")
    public void changeTo(String value) {
        if (this._key instanceof String) {
            this._key = (K) value;
        } else if (this._key instanceof Integer) {
            this._key = (K) new Integer(value);
        }
    }

    @Override
    public int hashCode() {
        return this._key.hashCode();
    }

    @Override
    public int compareTo(IKey o) {
        if (o == null || this._key == null)
            return 0;
        return this._key.toString().compareTo(o.toString());
    }

    @Override
    public boolean isEmpty() {
        return this._key == null || StringUtils.isEmpty(this._key.toString());
    }

    @Override
    public boolean isNotEmpty() {
        return this._key != null && StringUtils.isNotEmpty(this._key.toString());
    }

    @Override
    public boolean equals(Object key) {
        if (this._key == null || key == null)
            return false;
        return this._key.toString().equals(key.toString());
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
