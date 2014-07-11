/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.facet;


/**
 * <p>
 * Facet context for a stencil.
 * </p>
 * <p>
 * A facet context defines a context to retrieve the good presentation facet
 * from a stencil:
 * <ul>
 * <li>a facet type (html, flex, model, ..)
 * <li>a facet mode (user defined),
 * </ul>
 * </p>
 */

public class FacetContext implements Cloneable {

	private String _type; // facet type
	private String _mode; // facet mode

	public FacetContext(String facet, String mode) {
		setFacetType(facet);
		setFacetMode(mode);
	}

	/**
	 * @return the facet type (label, panel, tree, model, flex, ...)
	 */
	public final String getFacetType() {
		return this._type;
	}

	public final void setFacetType(String facet) {
		this._type = facet;
	}

	/**
	 * @return the facet mode.
	 */
	public final String getFacetMode() {
		return this._mode;
	}

	/**
	 * Sets the facet mode.
	 */
	public final void setFacetMode(String mode) {
		this._mode = mode;
	}

	@Override
	public FacetContext clone() {
		try {
			return (FacetContext) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("cannot clone FacetContext");
		}
	}

	@Override
	public String toString() {
		return String.format("facet: %s, mode: %s", this._type, this._mode);
	}

}