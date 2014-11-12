/**
 * Copyright GDO - 2005
 */
package com.gdo.context.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public abstract class _FileStcl extends Stcl {

    public interface Slot extends Stcl.Slot {

        /**
         * Context from which this file is defined.
         */
        String CONTEXT = "Context";

        /**
         * Property containing the path to access this file in context.
         */
        String PATH = "Path";

        /**
         * Property containing the absolute path in the file system.
         */
        String ABSOLUTE_PATH = "AbsolutePath";

        /**
         * Property containing the content as text.
         */
        String SIZE = "Size";

        /**
         * Last modification date (use the key as date format) as a string.<br>
         * If no key given then the date is integer value
         */
        String LAST_MODIFIED = "LastModified";
    }

    public interface Command extends Stcl.Command {
        String COPY = "Copy";
        String RENAME = "Rename";
        String DELETE = "Delete";
    }

    public _FileStcl(StclContext stclContext) {
        super(stclContext);
    }

}
