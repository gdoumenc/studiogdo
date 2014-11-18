/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.facet;

/**
 * <p>
 * Element of the facets descriptor file.
 * </p>
 */
public final class FacetDescriptor {

    private boolean _default; // is the default descriptor
    private boolean _container; // must be used when is a container
    private String _mode; // user mode defined
    private String _value; // CDATA part
    private String _file; // file associated (exclude value)
    private String _extends; // extension defined
    private String _same; // same facet as another mode
    private String _profile; // profile expression

    public boolean isDefault() {
        return _default;
    }

    public void setDefault(boolean def) {
        _default = def;
    }

    public boolean asContainer() {
        return _container;
    }

    public void setContainer(boolean container) {
        _container = container;
    }

    public String getMode() {
        return _mode;
    }

    public void setMode(String mode) {
        _mode = mode;
    }

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    public String getFile() {
        return _file;
    }

    public void setFile(String file) {
        _file = file;
    }

    public String getExtends() {
        return _extends;
    }

    public void setExtends(String xtends) {
        _extends = xtends;
    }

    public String getSame() {
        return _same;
    }

    public void setSame(String same) {
        _same = same;
    }

    public String getProfile() {
        return _profile;
    }

    public void setProfile(String profile) {
        _profile = profile;
    }
}
