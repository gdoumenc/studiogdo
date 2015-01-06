/**
 * Copyright GDO - 2004
 */
package com.gdo.project.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Matcher;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Utility class on Stcl.
 * </p>
 * 
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * 
 * 
 * @author Guillaume Doumenc (<a>
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class StclUtils {

    private static Pattern _pattern;

    static {
        try {
            // _pattern = new
            // Perl5Compiler().compile("([^£]*)(£)([^£]*)(£)(.*)",
            // Perl5Compiler.SINGLELINE_MASK);
        } catch (Exception e) {
            System.out.println("Format pattern error");
            _pattern = null;
        }
    }

    private StclUtils() {
        // utility class, disable instanciation
    }

    /*
     * public static StclIterator iterator(Result result) { if
     * (result.isSuccess()) { return iterator(); } return (StclIterator) new
     * NullStencilIterator<StclContext, PStcl>(result); }
     * 
     * public static StclIterator iterator(String msg) { return
     * iterator(Result.error("com.gdo.stencils", 0, msg, null)); }
     * 
     * public static StclIterator iterator(StclContext stclContext, PStcl stencil)
     * {
     * 
     * // null stencil case if (StencilUtils.isNull(stencil)) { return
     * iterator(StencilUtils.getNullReason(stencil)); }
     * 
     * // just a single iterator return (StclIterator) new
     * SingleIterator<StclContext, PStcl>(stencil); }
     */

    public static <S extends _PStencil<StclContext, S>> String expand£(StclContext stclContext, String text, _PStencil<StclContext, S> plugged) {
        if (_pattern == null)
            return text;

        // decode £
        String str = text.replaceAll("%A3", "£");

        // £name£ found then no extension
        PatternMatcher matcher = new Perl5Matcher();
        PatternMatcherInput input = new PatternMatcherInput(str);
        if (!matcher.contains(input, _pattern))
            return str;

        // extends all £name£
        StringBuffer res = new StringBuffer();
        MatchResult match;
        do {
            match = matcher.getMatch();
            res.append(match.group(1));
            String path = match.group(3);
            if (!StringUtils.isEmpty(path)) {
                res.append("<$stencil path=\"").append(path).append("\" mode=\"label\"/>");
            }
            input = new PatternMatcherInput(match.group(5));
        } while (matcher.contains(input, _pattern));
        return res.append(match.group(5)).toString();
    }

}
