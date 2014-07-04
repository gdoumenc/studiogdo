/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.facet;

/**
 * <p>
 * Element of the facets descriptor file.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
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
		return this._default;
	}

	public void setDefault(boolean def) {
		this._default = def;
	}

	public boolean asContainer() {
		return this._container;
	}

	public void setContainer(boolean container) {
		this._container = container;
	}

	public String getMode() {
		return this._mode;
	}

	public void setMode(String mode) {
		this._mode = mode;
	}

	public String getValue() {
		return this._value;
	}

	public void setValue(String value) {
		this._value = value;
	}

	public String getFile() {
		return this._file;
	}

	public void setFile(String file) {
		this._file = file;
	}

	public String getExtends() {
		return this._extends;
	}

	public void setExtends(String xtends) {
		this._extends = xtends;
	}

	public String getSame() {
		return this._same;
	}

	public void setSame(String same) {
		this._same = same;
	}

	public String getProfile() {
		return this._profile;
	}

	public void setProfile(String profile) {
		this._profile = profile;
	}
}
