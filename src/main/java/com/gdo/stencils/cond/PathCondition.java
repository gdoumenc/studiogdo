/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.cond;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.ConverterHelper;
import com.gdo.stencils.Keywords;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.util.PathUtils;

/**
 * Condition defined by the path as having a specific key or property.
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 */
public class PathCondition<C extends _StencilContext, S extends _PStencil<C, S>> extends StencilCondition<C, S> {

	private final String _pathCondition; // as (key) or [prop oper value]

	public PathCondition(C stclContext, String pathCondition, S stcl) {
		if (StringUtils.isNotBlank(pathCondition) && pathCondition.startsWith("({")) {
			String path = pathCondition.substring(2, pathCondition.length() - 2);
			_pathCondition = "(" + stcl.getString(stclContext, path, "") + ")";
		} else {
			_pathCondition = pathCondition;
		}
	}

	/**
	 * Creates a path condition on a key.
	 * 
	 * @param key
	 *          the key for the condition.
	 * @return the key path condition created.
	 */
	public static final <C extends _StencilContext, S extends _PStencil<C, S>> PathCondition<C, S> newKeyCondition(C stclContext, IKey key, S stcl) {
		return new PathCondition<C, S>(stclContext, PathUtils.KEY_SEP_OPEN + key.toString() + PathUtils.KEY_SEP_CLOSE, stcl);
	}

	@Deprecated
	public static final <C extends _StencilContext, S extends _PStencil<C, S>> PathCondition<C, S> newKeyCondition(C stclContext, String key, S stcl) {
		return new PathCondition<C, S>(stclContext, PathUtils.KEY_SEP_OPEN + key + PathUtils.KEY_SEP_CLOSE, stcl);
	}

	@Deprecated
	public static final <C extends _StencilContext, S extends _PStencil<C, S>> PathCondition<C, S> newKeyCondition(C stclContext, int key, S stcl) {
		return new PathCondition<C, S>(stclContext, PathUtils.KEY_SEP_OPEN + Integer.toString(key) + PathUtils.KEY_SEP_CLOSE, stcl);
	}

	/**
	 * Creates a path condition on an expression (the expression is of form [prop
	 * oper value]).
	 * 
	 * @param exp
	 *          the expression condition.
	 * @return the expression path condition created.
	 */
	public static final <C extends _StencilContext, S extends _PStencil<C, S>> PathCondition<C, S> newExpCondition(C stclContext, String exp, S stcl) {
		return new PathCondition<C, S>(stclContext, PathUtils.EXP_SEP_OPEN + exp + PathUtils.EXP_SEP_CLOSE, stcl);
	}

	/**
	 * Returns the key of this condition if it is a path condition on key value.
	 * 
	 * @return the key or <tt>null</tt> if not a key path condition.
	 */
	public static final <C extends _StencilContext, S extends _PStencil<C, S>> String getKeyCondition(StencilCondition<C, S> cond) {
		if (cond == null || !(cond instanceof PathCondition<?, ?>)) {
			return null;
		}
		String c = ((PathCondition<?, ?>) cond).getCondition();
		if (!PathUtils.isKeyContained(c)) {
			return null;
		}

		// TODO Should test ({ to evaluate..
		return PathUtils.getKeyContained(c);
	}

	public static final <C extends _StencilContext, S extends _PStencil<C, S>> String getExpPropCondition(StencilCondition<C, S> cond) {
		if (cond == null || !(cond instanceof PathCondition<?, ?>)) {
			return null;
		}
		String c = ((PathCondition<?, ?>) cond).getCondition();
		if (!PathUtils.isExpContained(c)) {
			return null;
		}

		return PathUtils.getPropContained(c);
	}

	public static final <C extends _StencilContext, S extends _PStencil<C, S>> String getExpValueCondition(StencilCondition<C, S> cond) {
		if (cond == null || !(cond instanceof PathCondition<?, ?>)) {
			return null;
		}
		String c = ((PathCondition<?, ?>) cond).getCondition();
		if (!PathUtils.isExpContained(c)) {
			return null;
		}

		return PathUtils.getValueContained(c);
	}

	public String getCondition() {
		return _pathCondition;
	}

	@Override
	public boolean verify(C stclContext, S stencil) {

		// key defined
		if (PathUtils.isKeyContained(_pathCondition)) {
			boolean negation = false;

			// does nothing on empty stencil
			if (stencil.isNull()) {
				return false;
			}

			// if no key on the stencil (only one stencil in slot, then accept
			// any key)
			String key = stencil.getKey().toString();
			if (StringUtils.isEmpty(key))
				return true;

			// get tested key (if empty, accept all stencils)
			String exp = PathUtils.getKeyContained(_pathCondition);
			if (exp.startsWith(NOT)) {
				negation = true;
				exp = exp.substring(1);
			}
			if (StringUtils.isEmpty(exp))
				return negation;

			// allow $ in key which is not a regular expression
			if (exp.startsWith("$"))
				exp = "\\" + exp;

			// composed expression
			return (negation) ? !key.matches(exp) : key.matches(exp);
		}

		// expression is slot = value
		if (PathUtils.isExpContained(_pathCondition)) {
			String exp = PathUtils.getExpContained(_pathCondition);
			if (exp.startsWith("$")) {
				return true; // link condition but also all stencils
			}

			// does nothing on empty stencil
			if (stencil.isNull()) {
				return false;
			}

			// get the slot tested
			String slot = PathUtils.getPropContained(_pathCondition);

			// facet value tested
			int index = slot.indexOf('$');
			if (index != -1) {
				String type = slot.substring(0, index);
				String mode = slot.substring(index + 1);
				if (StringUtils.isEmpty(mode))
					mode = "";
				String format = String.format("<$stencil path=\".\" facet=\"%s\" mode=\"%s\"/>", type, mode);
				String value = stencil.format(stclContext, format);
				return verifyPropertyValue(stclContext, value);
			}

			// TODO:command value returned tested (not done)
			if (slot.startsWith(PathUtils.COMMAND_OPER)) {
				String cmd = slot.substring(1);
				return verifyPropertyValue(stclContext, cmd);
			}

			// compares value
			String prop = stencil.getString(stclContext, slot, "");
			String value = PathUtils.getValueContained(_pathCondition);
			String type = stencil.getType(stclContext, slot);
			if (StringUtils.isNotBlank(value) && value.startsWith("{")) {
				value = stencil.getString(stclContext, value.substring(1, value.length() - 1), "");
			}
			String oper = PathUtils.getOperContained(_pathCondition);
			return compare(type, prop, value, oper);
		}
		return false;
	}

	@Override
	public String toSQL(C stclContext, String alias, S stencil) {

		// key condition for sql slot
		if (PathUtils.isKeyContained(_pathCondition)) {
			try {

				// if the expression is a number, then this number should be the id
				String exp = PathUtils.getKeyContained(_pathCondition);

				// if contains order or limit then no condition
				if (exp.toLowerCase().indexOf("order") != -1 || exp.toLowerCase().indexOf("limit") != -1) {
					return "";
				}

				// the key is the id
				int id = Integer.parseInt(exp.trim());
				if (StringUtils.isNotBlank(alias)) {
					return String.format("`%s`.Id='%s'", alias, id);
				}
				return String.format("Id='%s'", id);
			} catch (NumberFormatException e) {
				return String.format("PathCondition:toSQL %s", e);
			}
		} else if (PathUtils.isExpContained(_pathCondition)) {

			// get the slot tested
			String slot = PathUtils.getPropContained(_pathCondition);

			// compares value
			String value = PathUtils.getValueContained(_pathCondition);
			if (StringUtils.isNotBlank(value) && value.startsWith("{")) {
				value = stencil.getString(stclContext, value.substring(1, value.length() - 1), "");
			}
			String oper = PathUtils.getOperContained(_pathCondition);
			if (StringUtils.isNotBlank(alias)) {
				return String.format("`%s`.%s %s '%s'", alias, slot, toSQL(oper), value);
			}
			return String.format("%s %s '%s'", slot, toSQL(oper), value);
		}
		return "PathCondition:toSQL NOT DONE";
	}

	@Deprecated
	protected boolean verifyPropertyValue(C stclContext, String prop) {
		String value = PathUtils.getValueContained(_pathCondition);
		String oper = PathUtils.getOperContained(_pathCondition);
		if (oper != null) {
			if (oper.equals(PathUtils.EQUALS_OPER)) {
				return (prop.equals(value));
			}
			if (oper.equals(PathUtils.DIFF_OPER)) {
				return (!prop.equals(value));
			}
			if (oper.equals(PathUtils.STARTS_WITH_OPER) && value.length() > 0) {
				return (prop.startsWith(value));
			}
			if (oper.equals(PathUtils.ENDS_WITH_OPER) && value.length() > 0) {
				return (prop.endsWith(value));
			}
			if (oper.equals(PathUtils.MATCHES_OPER) && value.length() > 0) {
				return (prop.matches(value));
			}
		}
		return false;
	}

	/**
	 * Global function for comparision used also by command.
	 * 
	 * @param type
	 *          comparision type (string, int, boolean).
	 * @param prop
	 *          the property value to be tested.
	 * @param value
	 *          the value to be tested.
	 * @param operator
	 *          the testing operator.
	 * @return
	 */
	public static final boolean compare(String type, String prop, String value, String operator) {
		if (type.equals(Keywords.STRING)) {
			return compareString(prop, value, operator);
		} else if (type.equals(Keywords.INT)) {
			return compareInt(prop, value, operator);
		} else if (type.equals(Keywords.BOOLEAN)) {
			return compareBoolean(prop, value, operator);
		}
		return false;
	}

	private static final boolean compareString(String propValue, String value, String operator) {
		if (propValue == null)
			return false;
		if (operator.equals(PathUtils.EQUALS_OPER)) {
			return (propValue.equals(value));
		}
		if (operator.equals(PathUtils.DIFF_OPER)) {
			return (!propValue.equals(value));
		}
		if (operator.equals(PathUtils.STARTS_WITH_OPER) && value.length() > 0) {
			return (propValue.startsWith(value));
		}
		if (operator.equals(PathUtils.ENDS_WITH_OPER) && value.length() > 0) {
			return (propValue.endsWith(value));
		}
		if (operator.equals(PathUtils.MATCHES_OPER) && value.length() > 0) {
			return (propValue.matches(value));
		}
		return false;
	}

	private static final boolean compareInt(String prop, String value, String operator) {
		int propValue = Integer.parseInt(prop);
		int compValue = Integer.parseInt(value);

		if (operator.equals(PathUtils.EQUALS_OPER)) {
			return (propValue == compValue);
		}
		if (operator.equals(PathUtils.DIFF_OPER)) {
			return (propValue != compValue);
		}
		if (operator.equals(">")) {
			return (propValue > compValue);
		}
		if (operator.equals(">=")) {
			return (propValue >= compValue);
		}
		if (operator.equals("<")) {
			return (propValue < compValue);
		}
		if (operator.equals("<=")) {
			return (propValue <= compValue);
		}
		return false;
	}

	private static final boolean compareBoolean(String prop, String value, String operator) {
		boolean propValue = ConverterHelper.parseBoolean(prop);
		boolean compValue = ConverterHelper.parseBoolean(value);

		if (operator.equals(PathUtils.EQUALS_OPER)) {
			return (propValue == compValue);
		}
		if (operator.equals(PathUtils.DIFF_OPER)) {
			return (propValue != compValue);
		}
		return false;
	}

	private static final String toSQL(String operator) {
		if (operator.equals(PathUtils.EQUALS_OPER)) {
			return "=";
		}
		if (operator.equals(PathUtils.DIFF_OPER)) {
			return "!=";
		}
		return "Unknown SQL operator";
	}

	@Override
	public String toString() {
		return _pathCondition;
	}

}
