package com.gdo.stencils.atom;

/**
 * <p>
 * Interface for an atom : an object with an unique identifier and comparable.
 * </p>
 * <p>
 * This identifier is used to unically reference this atom in any XML
 * description file.
 * </p>
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@coworks.pro">gdoumenc@studiogdo.com)</a>
 */
public interface IAtom<C, S> {

    /**
     * @return this object casted to the generic signature.
     */
    public S self();

    /**
     * @return the session identifier for this atom.
     */
    String getId(C stclContext);

    /**
     * @return the unique internal identificator for this atom for store
     *         purpose.
     */
    String getUId(C stclContext);

}