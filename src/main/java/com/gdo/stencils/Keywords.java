/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils;

/**
 * Common model for presentation mode. A stencil must be attached to determine
 * default value in properties file.
 */
public interface Keywords {

	public static final String _ = ".";

	public static final String AFTER = "after";
	public static final String ARITY = "arity";
	public static final String ALIGN = "align";
	public static final String ALWAYS = "always";
	public static final String ANCHORS = "anchors";
	public static final String BOOLEAN = "boolean";
	public static final String BUTTONS = "buttons";
	public static final String CALCULATED = "calculated";
	public static final String CHECK = "check";
	public static final String CLASS = "class";
	public static final String COLUMN = "column";
	public static final String COLUMNS = "columns";
	public static final String COMMAND = "command";
	public static final String COMMANDS = "commands";
	public static final String CONCAT = "concat";
	public static final String CONTAINS = "contains";
	public static final String CREATE = "create";
	public static final String DEFAULT = "default";
	public static final String DELEGATE = "delegate";
	public static final String DRAG = "drag";
	public static final String DRAG_SELECTED = "drag" + _ + "selected";
	public static final String DROP_SELECTED = "drop" + _ + "selected";
	public static final String ERR = "err";
	public static final String ENABLE = "enable";
	public static final String EXPANDED = "expanded";
	public static final String FILE = "file";
	public static final String FIXED = "fixed";
	public static final String FOLDERS = "folders";
	public static final String FORMAT = "format";
	public static final String FROM = "from";
	public static final String FULL_PAGE = "fullPage";
	public static final String GNIRTS = "gnirts";
	public static final String GRID = "grid";
	public static final String HEAD = "head";
	public static final String HEIGHT = "height";
	public static final String HELP = "help";
	public static final String HREF = "href";
	public static final String HTML = "html";
	public static final String ID = "id";
	public static final String IMAGE = "image";
	public static final String IMAGES = "images";
	public static final String INT = "int";
	public static final String KEY = "key";
	public static final String LABEL = "label";
	public static final String LABELS = "labels";
	public static final String LAYOUT = "layout";
	public static final String LOAD = "load";
	public static final String LOWER = "lower";
	public static final String LIST = "list";
	public static final String LINKS = "links";
	public static final String MENU = "menu";
	public static final String MODE = "mode";
	public static final String NODES = "nodes";
	public static final String NONE = "none";
	public static final String NUMBER = "number";
	public static final String MSG = "msg";
	public static final String NEVER_EMPTY = "neverEmpty";
	public static final String ORDERED = "ordered";
	public static final String OK = "ok";
	public static final String PANEL = "panel";
	public static final String PANELS = "panels";
	public static final String PARAM = "param";
	public static final String PARENT = "parent";
	public static final String PATH = "path";
	public static final String PLUG = "plug";
	public static final String POSITION = "position";
	public static final String PREFIX = "prefix";
	public static final String PROFILE = "profile";
	public static final String READ_ONLY = "readOnly";
	public static final String REF = "ref";
	public static final String REPLACE = "replaceWithoutStencil";
	public static final String RESULT = "result";
	public static final String REWOL = "rewol";
	public static final String ROOT = "root";
	public static final String ROW = "row";
	public static final String ROWS = "rows";
	public static final String ROW_SLOTS = "rowSlots";
	public static final String SECRET = "secret";
	public static final String SELECTED = "selected";
	public static final String SELECTOR = "selector";
	public static final String SELECT_SLOTS = "selectSlots";
	public static final String TREE_SLOTS = "treeSlots";
	public static final String SCROLLED = "scrolled";
	public static final String SIMPLE_SLOTS = "simpleSlots";
	public static final String SINGLE = "single";
	public static final String SLOTS = "slots";
	public static final String STENCIL = "stencil";
	public static final String STRING = "string";
	public static final String STYLE = "style";
	public static final String TAB = "tab";
	public static final String TAIL = "tail";
	public static final String TARGET = "target";
	public static final String TEXT = "text";
	public static final String TEXTAREA = "textarea";
	public static final String TNI = "tni";
	public static final String THIS = "THIS";
	public static final String TIP = "tip";
	public static final String TITLE = "title";
	public static final String TRACE = "trace";
	public static final String TREE = "tree";
	public static final String TYPE = "type";
	public static final String TRANSIENT = "transient";
	public static final String UNCHECK = "uncheck";
	public static final String UNIQUE = "unique";
	public static final String UNPLUG = "unplug";
	public static final String UPDATE = "update";
	public static final String UPLOADS = "uploads";
	public static final String VALUE = "value";
	public static final String VISIBLE = "visible";
	public static final String WIDTH = "width";
	public static final String XINHA = "xinha";

	public static final String DEFAULT_PARAM1 = DEFAULT + _ + PARAM + "1";
	public static final String DEFAULT_PARAM2 = DEFAULT + _ + PARAM + "2";
	public static final String DEFAULT_PARAM3 = DEFAULT + _ + PARAM + "3";

	public static final String DEFAULT_OK = DEFAULT + _ + OK;
	public static final String PARAM_NUMBER = PARAM + _ + NUMBER;
	public static final String PARAM0 = PARAM + "0";
	public static final String PARAM1 = PARAM + "1";
	public static final String PARAM2 = PARAM + "2";
	public static final String PARAM3 = PARAM + "3";
	public static final String PARAM4 = PARAM + "4";
	public static final String PARAM5 = PARAM + "5";
	public static final String OK_NUMBER = OK + _ + NUMBER;
	public static final String OK0 = OK + "0";
	public static final String OK1 = OK + "1";
	public static final String OK2 = OK + "2";
	public static final String OK3 = OK + "3";
	public static final String DEFAULT_ERR = DEFAULT + _ + ERR;
	public static final String ERR_NUMBER = ERR + _ + NUMBER;
	public static final String ERR0 = ERR + "0";
	public static final String ERR1 = ERR + "1";
	public static final String ERR2 = ERR + "2";
	public static final String ERR3 = ERR + "3";

	public static final String ERR_MSG = ERR + _ + MSG;
	public static final String HELP_FORMAT = HELP + _ + FORMAT;
	public static final String SUPER_HIDE = "super" + _ + "hide";
	public static final String TREE_ITEM = "tree" + _ + "item";
	public static final String TREE_SELECTOR = "tree" + _ + "selector";
	public static final String FORMAT_STENCIL = "format" + _ + "stencil";
	public static final String POSITION_ROW = POSITION + _ + ROW;
	public static final String POSITION_COLUMN = POSITION + _ + COLUMN;
	public static final String MODE_TREE = MODE + _ + TREE;
	public static final String MODE_PANEL = MODE + _ + PANEL;
	public static final String MODE_TAB = MODE + _ + TAB;

	public static final String ROW_CHECK = ROW + _ + CHECK;
	public static final String SCROLLED_WIDTH = SCROLLED + _ + WIDTH;
	public static final String SCROLLED_HEIGHT = SCROLLED + _ + HEIGHT;

	// CSS
	public static final String RIGHT = "right";
	public static final String LEFT = "left";
	public static final String BOTTOM = "bottom";
	public static final String TOP = "top";
	public static final String CENTER = "center";
	public static final String NORTH = "north";
	public static final String SOUTH = "south";
	public static final String WEST = "west";
	public static final String EAST = "east";
	public static final String VALIGN = "valign";

	public static final String BORDER = "border";
	public static final String BORDER_LEFT = BORDER + _ + LEFT;
	public static final String BORDER_RIGHT = BORDER + _ + RIGHT;
	public static final String BORDER_BOTTOM = BORDER + _ + BOTTOM;
	public static final String BORDER_TOP = BORDER + _ + TOP;

	public static final String PADDING = "padding";
	public static final String PADDING_LEFT = PADDING + _ + LEFT;
	public static final String PADDING_RIGHT = PADDING + _ + RIGHT;
	public static final String PADDING_BOTTOM = PADDING + _ + BOTTOM;
	public static final String PADDING_TOP = PADDING + _ + TOP;

}