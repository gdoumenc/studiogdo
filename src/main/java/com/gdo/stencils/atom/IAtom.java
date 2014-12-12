package com.gdo.stencils.atom;

import com.gdo.stencils._StencilContext;

/**
 * <p>
 * Interface for an atom : an object with an unique identifier and comparable.
 * </p>
 * <p>
 * This identifier is used to unically reference this atom in any XML
 * description file.
 * </p>
 */
public interface IAtom<S> {

    /**
     * @return this object casted to the generic signature.
     */
    public S self();

    /**
     * @return the session identifier for this atom.
     */
    String getId(_StencilContext stclContext);

    /**
     * @return the unique internal identificator for this atom for store
     *         purpose.
     */
    String getUId(_StencilContext stclContext);

}