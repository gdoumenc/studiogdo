/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.facet;

/**
 * <p>
 * Facet types for stencil.
 * </p>
 */
public interface FacetType {

    public static final String NONE = "none";
    public static final String DEBUG = "debug";

    public static final String LABEL = "label";
    public static final String PANEL = "panel"; // html
    public static final String TREE = "tree"; // html tree

    public static final String FLEX = "flex";
    public static final String MODEL = "model"; // tree flex model

    public static final String HTML = "html";
    public static final String HTML5 = "html5";
    public static final String DOM5 = "dom5";
    public static final String HTML5_TEXT = "html5text";
    public static final String DOM5_TEXT = "dom5text";
    public static final String SKEL5 = "skel5";
    public static final String JSON = "json";
    public static final String JSKEL = "jskel";
    public static final String PYTHON = "python";
    public static final String REST = "rest";
    public static final String TRANS = "trans";

    public static final String FILE = "file";
    public static final String BYTES = "bytes";
    public static final String CSV = "csv";
    public static final String E4X = "e4x";
    public static final String PDF = "pdf";
    public static final String XLS = "xls";

    public static final String ENUM = "enum"; // the value is transformed in
    // enumerated label
    public static final String MASK = "mask"; // generator interface
    public static final String PRINT = "print";

}
