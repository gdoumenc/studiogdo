/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.cond;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils._StencilContext;
import com.gdo.stencils.key.Key;
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
public abstract class LinkCondition {

    // get all stenicls and links at it (not following them)
    public static <C extends _StencilContext, S extends _PStencil<C, S>> StencilCondition<C, S> withLinksCondition(C stclContext, S stcl) {
        return PathCondition.newExpCondition(stclContext, "$", stcl);
    }

    // alls tencils but not links
    public static <C extends _StencilContext, S extends _PStencil<C, S>> StencilCondition<C, S> withoutLinksCondition(C stclContext, S stcl) {
        return PathCondition.newKeyCondition(stclContext, new Key("$!"), stcl);
    }

    // validate only links
    public static <C extends _StencilContext, S extends _PStencil<C, S>> StencilCondition<C, S> onlyLinksCondition(C stclContext, S stcl) {
        return PathCondition.newKeyCondition(stclContext, new Key("$"), stcl);
    }

    public static boolean isLinkKey(String key) {
        return (StringUtils.isNotEmpty(key) && key.startsWith("$"));
    }

    /**
     * Tests if a condition is to retrieve the adaptor directly.
     */
    public static <C extends _StencilContext, S extends _PStencil<C, S>> boolean isWithLinksCondition(C stclContext, S link, StencilCondition<C, S> cond) {
        if (cond instanceof PathCondition) {
            String path = ((PathCondition<C, S>) cond).getCondition();
            if (PathUtils.isKeyContained(path)) {
                String key = PathUtils.getKeyContained(path);
                if (StringUtils.isNotEmpty(key) && key.startsWith("$")) {
                    String c = key.substring(1);
                    if (StringUtils.isEmpty(c))
                        return true;
                    PathCondition<C, S> p = PathCondition.newKeyCondition(stclContext, new Key(c), link);
                    return p.verify(stclContext, link);
                }
            } else if (PathUtils.isExpContained(path)) {
                String exp = PathUtils.getExpContained(path);
                if (StringUtils.isNotEmpty(exp) && exp.startsWith("$")) {
                    String c = exp.substring(1);
                    if (StringUtils.isEmpty(c))
                        return true;
                    PathCondition<C, S> p = PathCondition.newExpCondition(stclContext, c, link);
                    return p.verify(stclContext, link);
                }
            }
        }
        return false;
    }

    public static <C extends _StencilContext, S extends _PStencil<C, S>> boolean isWithoutLinksCondition(C stclContext, S link, StencilCondition<C, S> cond) {
        if (cond instanceof PathCondition) {
            String path = ((PathCondition<C, S>) cond).getCondition();
            if (PathUtils.isExpContained(path)) {
                String exp = PathUtils.getExpContained(path);
                return (StringUtils.isNotEmpty(exp) && exp.equals("$!"));
            }
        }
        return false;
    }

    public static <C extends _StencilContext, S extends _PStencil<C, S>> boolean isOnlyLinksCondition(C stclContext, S link, StencilCondition<C, S> cond) {
        if (cond instanceof PathCondition) {
            String path = ((PathCondition<C, S>) cond).getCondition();
            if (PathUtils.isKeyContained(path)) {
                String key = PathUtils.getKeyContained(path);
                return (StringUtils.isNotEmpty(key) && key.equals("$"));
            }
        }
        return false;
    }

}
