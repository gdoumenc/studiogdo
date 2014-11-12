package com.gdo.stencils.key;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.log.StencilLog;

public class Key implements IKey {

    // const NO_KEY
    public static final IKey NO_KEY = new Key(StringHelper.EMPTY_STRING) {
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

    private String _key; // the real key

    public Key(String value) {
        if (value == null) {
            String msg = logError(null, "creation of a key with null value");
            throw new NullPointerException(msg);
        }
        _key = value;
    }

    public Key(int value) {
        this(Integer.toString(value));
    }

    @Override
    public String toString() {
        return _key;
    }

    public String getValue() {
        return _key;
    }

    public void changeTo(String value) {
        _key = value;
    }

    @Override
    public int hashCode() {
        return _key.hashCode();
    }

    @Override
    public int compareTo(IKey key) {
        if (key == null)
            return 0;
        return _key.compareTo(key.toString());
    }

    @Override
    public boolean equals(Object key) {
        if (key == null)
            return false;
        return _key.equals(key.toString());
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isBlank(_key);
    }

    @Override
    public boolean isNotEmpty() {
        return StringUtils.isNotBlank(_key);
    }

    //
    // LOG PART
    //

    private StencilLog getLog() {
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
