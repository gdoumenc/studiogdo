/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.gdo.helper.ClassHelper;
import com.gdo.project.adaptor.ISlotEmulator;
import com.gdo.stencils.Result;
import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.LinkCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.iterator.EmptyIterator;
import com.gdo.stencils.iterator.ListIterator;
import com.gdo.stencils.iterator.SingleIterator;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;

/**
 * <p>
 * Stencil utility class.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo & Guillaume Doumenc. Use
 * is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */
public class StencilUtils {

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String EMPTY_REASON = "empty stencil without any reason";

	private StencilUtils() {
		// utility class, disable instanciation
	}

	/**
	 * Creates a null plugged stencil with the reason why.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param reasons
	 *          the error results which produced the null plugged stencil.
	 * @return a null plugged stencil.
	 */
	public static <C extends _StencilContext, S extends _PStencil<C, S>> S nullPStencil(C stclContext, Result reasons) {
		StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
		Class<? extends S> c = factory.getDefaultPStencilClass(stclContext);
		return ClassHelper.newInstance(c, reasons);
	}

	/**
	 * 
	 * Checks if a stencil is not defined.
	 * 
	 * @param pstcl
	 *          the stencil checked.
	 * @return <tt>true</tt> if the stencil is not defined.
	 */
	public static final <C extends _StencilContext, S extends _PStencil<C, S>> boolean isNull(_PStencil<C, S> pstcl) {
		return (pstcl == null) || pstcl.isNull();
	}

	/**
	 * 
	 * Checks if a slot is not defined.
	 * 
	 * @param pslot
	 *          the slot checked.
	 * @return <tt>true</tt> if the slot is not defined.
	 */
	public static final <C extends _StencilContext, S extends _PStencil<C, S>> boolean isNull(PSlot<C, S> pslot) {
		return (pslot == null) || pslot.isNull();
	}

	/**
	 * 
	 * Checks if a stencil is defined.
	 * 
	 * @param pstcl
	 *          the stencil checked.
	 * @return <tt>true</tt> if the stencil is defined.
	 */
	public static final <C extends _StencilContext, S extends _PStencil<C, S>> boolean isNotNull(_PStencil<C, S> pstcl) {
		return (pstcl != null) && pstcl.isNotNull();
	}

	/**
	 * 
	 * Checks if a slot is defined.
	 * 
	 * @param pslot
	 *          the slot checked.
	 * @return <tt>true</tt> if the stencil is defined.
	 */
	public static final <C extends _StencilContext, S extends _PStencil<C, S>> boolean isNotNull(PSlot<C, S> pslot) {
		return (pslot != null) && pslot.isNotNull();
	}

	/**
	 * getResult.
	 */
	public static final <C extends _StencilContext, S extends _PStencil<C, S>> Result getResult(_PStencil<C, S> stcl) {
		return (stcl == null) ? Result.error("no result...") : stcl.getResult();
	}

	/**
	 * Returns the reason why a stencil is null.
	 * 
	 * @param stcl
	 *          the null stencil.
	 * @return the reason why the stencil is null.
	 */
	public static final <C extends _StencilContext, S extends _PStencil<C, S>> String getNullReason(_PStencil<C, S> stcl) {
		if (stcl == null) {
			return EMPTY_REASON;
		}
		String reason = stcl.getNullReason();
		return (reason == null) ? EMPTY_REASON : reason;
	}

	/**
	 * Tests if two stencils are not null and are same.
	 * 
	 * @return <tt>true</tt> if the two stencils are defined and are same.
	 */
	public static final <C extends _StencilContext, S extends _PStencil<C, S>> boolean equals(_PStencil<C, S> stcl1, _PStencil<C, S> stcl2) {
		if (stcl1 != null)
			return stcl1.equals(stcl2);
		if (stcl2 != null)
			return stcl2.equals(stcl1);
		return false;
	}

    /**
     * Returns an empty stencil iterator.
     * 
     * @param result
     *          the reason why this iterator is empty.
     * @return an empty stencil iterator.
     */
    public static <C extends _StencilContext, S extends _PStencil<C, S>> StencilIterator<C, S> iterator(Result result) {
        return new EmptyIterator<C, S>(result);
    }

	/**
	 * Returns an empty stencil iterator.
	 * 
	 * @return an empty stencil iterator.
	 */
	public static <C extends _StencilContext, S extends _PStencil<C, S>> StencilIterator<C, S> iterator() {
		return StencilUtils.<C, S> iterator(Result.success());
	}

	/**
	 * Creates a stencil iterator made of one single stencil plugged in a slot. If
	 * stencil is <tt>null</tt> returns empty iterator.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param stencil
	 *          the stencil for iteration.
	 * @param slot
	 *          the slot of iteration.
	 * @return
	 */
	public static <C extends _StencilContext, S extends _PStencil<C, S>> StencilIterator<C, S> iterator(C stclContext, S stencil, PSlot<C, S> slot) {

		// null stencil case
		if (StencilUtils.isNull(stencil)) {
			return StencilUtils.<C, S> iterator(Result.error(StencilUtils.getNullReason(stencil)));
		}

		// single iterator on stencil if same containing slot
		if (SlotUtils.equals(stencil.getContainingSlot(), slot)) {
			return new SingleIterator<C, S>(stencil);
		}

		// single iterator (just changing containing slot if not same)
		StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
		S stcl = factory.createPStencil(stclContext, slot, stencil.getKey(), stencil);
		return new SingleIterator<C, S>(stcl);
	}

	/**
	 * Returns a list of slots defined from a multi-path (":" separated path).
	 * 
	 * @return the slots defined by a path from a stencil.
	 */
	public static <C extends _StencilContext, S extends _PStencil<C, S>> Iterator<PSlot<C, S>> getSlots(C stclContext, S stcl, String slotPath) {
		List<PSlot<C, S>> slots = new ArrayList<PSlot<C, S>>();
		for (String path : PathUtils.splitMultiPath(slotPath)) {

			// composed path
			if (PathUtils.isComposed(path)) {
				String first = PathUtils.getPathName(path);
				String tail = PathUtils.getLastName(path);
				String slot = PathUtils.getSlotPath(tail);
				for (S s : stcl.getStencils(stclContext, first)) {
					slots.add(s.getSlot(stclContext, slot));
				}
			} else {
				String slot = PathUtils.getSlotPath(path);
				PSlot<C, S> s = stcl.getSlot(stclContext, slot);
				if (SlotUtils.isNotNull(s))
					slots.add(s);
			}
		}
		return slots.iterator();
	}

	/**
	 * @return a stencil iterator from a list of stencils. If stencil is
	 *         <tt>null</tt> return empty iterator. If condition is <tt>null</tt>
	 *         return an iterator on list.
	 */
	// /// DEPRECATD
	@SuppressWarnings("unchecked")
	public static <C extends _StencilContext, S extends _PStencil<C, S>> StencilIterator<C, S> iterator(C stclContext, Iterator<S> list, StencilCondition<C, S> cond, PSlot<C, S> slot) {

		// does nothing on empty iteration
		if (!list.hasNext()) {
			return StencilUtils.<C, S> iterator();
		}

		// creates the new plugged stencil iterator (follow link if needed)
		StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
		List<S> res = new ArrayList<S>();

		// for each stencil in the list
		synchronized (list) {
			while (list.hasNext()) {
				S stencil = list.next();
				if (stencil.isLink(stclContext)) {
					_Stencil<C, S> s = stencil.getReleasedStencil(stclContext);
					ISlotEmulator<C, S> l = (ISlotEmulator<C, S>) s;

					// if condition is to get the link itself or only stencils
					// from another link
					if (cond != null) {
						if (LinkCondition.isWithoutLinksCondition(stclContext, stencil, cond)) {
							continue;
						}
						if (LinkCondition.isWithLinksCondition(stclContext, stencil, cond)) {
							res.add(stencil);
							continue;
						}
					}

					// creates the new plugged link
					S link = factory.createPStencil(stclContext, slot, stencil.getKey(), stencil);
					StencilIterator<C, S> linked = l.getStencils(stclContext, cond, slot, link);
					if (linked.isNotValid()) {
						return linked;
					}
					for (S stcl : linked) {
						res.add(stcl);
					}

				}

				// simple stencil case
				else if (cond == null || (!LinkCondition.isOnlyLinksCondition(stclContext, stencil, cond) && cond.verify(stclContext, stencil))) {

					// does nothing if same slot
					// if (false && slot.equals(stencil.getContainingSlot())) {
					// res.add(stencil);
					// }

					// creates the new plugged stencil
					// else {
					S stcl = factory.createPStencil(stclContext, slot, stencil.getKey(), stencil);
					res.add(stcl);
					// }
				}
			}
		}

		// return the new list
		return new ListIterator<C, S>(res);
	}

	/**
	 * @return a stencil iterator from a list of stencils. If stencil is
	 *         <tt>null</tt> return empty iterator. If condition is <tt>null</tt>
	 *         return an iterator on list.
	 */
	@SuppressWarnings("unchecked")
	public static <C extends _StencilContext, S extends _PStencil<C, S>> StencilIterator<C, S> iterator(C stclContext, Collection<S> list, StencilCondition<C, S> cond, PSlot<C, S> slot) {

		// does nothing on empty iteration
		if (list.size() == 0) {
			return StencilUtils.<C, S> iterator();
		}

		// creates the new plugged stencil iterator (follow link if needed)
		StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
		Vector<S> res = new Vector<S>(list.size());

		// for each stencil in the list
		synchronized (list) {
			Iterator<S> iter = list.iterator();
			while (iter.hasNext()) {
				S stencil = iter.next();
				if (stencil.isLink(stclContext)) {
					_Stencil<C, S> s = stencil.getReleasedStencil(stclContext);
					ISlotEmulator<C, S> l = (ISlotEmulator<C, S>) s;

					// if condition is to get the link itself or only stencils
					// from another link
					if (cond != null) {
						if (LinkCondition.isWithoutLinksCondition(stclContext, stencil, cond)) {
							continue;
						}
						if (LinkCondition.isWithLinksCondition(stclContext, stencil, cond)) {
							res.add(stencil);
							continue;
						}
					}

					// creates the new plugged link
					S link = factory.createPStencil(stclContext, slot, stencil.getKey(), stencil);
					StencilIterator<C, S> linked = l.getStencils(stclContext, cond, slot, link);
					if (linked.isNotValid()) {
						return linked;
					}
					for (S stcl : linked) {
						res.add(stcl);
					}

				}

				// simple stencil case
				else if (cond == null || (!LinkCondition.isOnlyLinksCondition(stclContext, stencil, cond) && cond.verify(stclContext, stencil))) {

					// does nothing if same slot
					// if (false && slot.equals(stencil.getContainingSlot())) {
					// res.add(stencil);
					// }

					// creates the new plugged stencil
					// else {
					S stcl = factory.createPStencil(stclContext, slot, stencil.getKey(), stencil);
					res.add(stcl);
					// }
				}
			}
		}

		// returns the new list iterator
		res.trimToSize();
		return new ListIterator<C, S>(res);
	}

	/**
	 * Sorts the specified stencil iterator according to the order induced by the
	 * specified comparator.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @param iter
	 *          the stencil iterator to iterate over stencils.
	 * @param comparator
	 *          the stencil comparator to sort the iterator.
	 * @return aq new stencil ordered iterator.
	 */
	public static <C extends _StencilContext, S extends _PStencil<C, S>> StencilIterator<C, S> sort(C stclContext, StencilIterator<C, S> iter, Comparator<S> comparator) {
		List<S> list = new ArrayList<S>(iter.size());
		for (S stcl : iter) {
			list.add(stcl);
		}
		Collections.sort(list, comparator);
		return new ListIterator<C, S>(list);
	}

	public static boolean containsStencilTag(String string) {
		if (string.indexOf("<$stencil") != -1)
			return true;
		if (string.indexOf("&lt;$stencil") != -1)
			return true;
		if (string.indexOf("%3C$stencil") != -1)
			return true;
		return false;
	}

}