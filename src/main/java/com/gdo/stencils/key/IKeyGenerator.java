/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.key;

/**
 * <p>
 * Unique key generator interface for plugging stencil is a slot.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com)</a>
 */
public interface IKeyGenerator {

    /**
     * @return the unique key obtained.
     */
    IKey getKey();

}