/**
 * Copyright GDO - 2005
 */
package com.gdo.context.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;

public abstract class FileStcl extends Stcl {

    public interface Slot extends Stcl.Slot, _FileStcl.Slot {

        /**
         * Property containing the content as text.
         */
        String CONTENT = "Content";
        String ENCODED_CONTENT = "EncodedContent";

        /**
         * Property containing mime type.
         */
        String MIME_TYPE = "MimeType";
    }

    public interface Command extends Stcl.Command, _FileStcl.Command {
    }

    public FileStcl(StclContext stclContext) {
        super(stclContext);
    }
}
