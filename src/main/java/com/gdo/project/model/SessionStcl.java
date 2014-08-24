/**
 * Copyright GDO - 2005
 */
package com.gdo.project.model;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.gdo.servlet.RpcWrapper;
import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.WrongPathException;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.iterator.ListIterator;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.CalculatedIntegerPropertySlot;
import com.gdo.stencils.slot.CalculatedStringPropertySlot;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.slot.MultiSlot;
import com.gdo.stencils.slot._Slot;
import com.gdo.stencils.util.PathUtils;

/**
 * <p>
 * Session stencil defined for each servlet session.
 * </p>
 * <p>
 * The session stencil has any slots which are created on demand.
 * </p>
 * <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com</a>)
 */

public class SessionStcl extends Stcl implements HttpSessionBindingListener {

	public static final String SESSION_KEY = "SESSION_KEY";
	public static final String HITS_KEY = "HITS_KEY";

	// list of all session stencils for all projects (key is session id)
	public static Map<String, Stcl> SESSION_STENCILS = new HashMap<String, Stcl>();

	// list of all sessions defined for all projects (key is session id)
	public static Map<String, HttpSession> HTTP_SESSIONS = new ConcurrentHashMap<String, HttpSession>();

	// number of session stencils currently active
	private static int NUMBER = 0;

	// max number of session stencils active in the same time
	private static int MAX = 0;

	// number of session stencils created during all service life
	private static int MAX_NUMBER = 0;

	/**
	 * Retrieves the session stencil from HTTP session.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 * @return the session stencil.
	 */
	public static Stcl getSessionStcl(StclContext stclContext) {
		HttpSession session = stclContext.getHttpSession();
		return (Stcl) session.getAttribute(SESSION_KEY);
	}

	/**
	 * Creates and store the session stencil in HTTP session.
	 * 
	 * The session is stored as a stencil and not as a plugged stencil as it
	 * implements the HttpSessionBindingListener interface.
	 * 
	 * @param stclContext
	 *          the stencil context.
	 */
	public static void createSessionStcl(StclContext stclContext) {
		HttpSession session = stclContext.getHttpSession();
		HttpServletRequest request = stclContext.getRequest();

		// checks another one wasn't created before
		Stcl sessionStcl = getSessionStcl(stclContext);
		if (sessionStcl != null) {
			logError("A session stencil (%s) already exists in session %s (ip %s)",sessionStcl, session.getId(), request.getRemoteAddr());
		} else {
			StclFactory factory = (StclFactory) stclContext.getStencilFactory();
			sessionStcl = factory.createStencil(stclContext, SessionStcl.class);
		}

		// bounds it to the session
		Principal userPrincipal = request.getUserPrincipal();
		if (userPrincipal != null) {
			String name = userPrincipal.getName();
			logWarn("Session stencil created by \"%s\" in session %s", name, session.getId());
		} else {
			logWarn("Session stencil created in session %s", session.getId());
		}
		session.setAttribute(SESSION_KEY, sessionStcl);
	}

	public interface Slot extends Stcl.Slot {
		String ID = "Id";

		String SLOT_ORDER = "SlotOrder";
		String SLOT_LIMIT = "SlotLimit";

		String NUMBER = "Number";
		String MAX = "Max";
		String MAX_NUMBER = "MaxNumber";
		String HITS = "Hits";
		String SESSIONS = "Sessions";

		String THREADS = "Threads";
	}

	public SessionStcl(StclContext stclContext) {
		super(stclContext);

		new IdSlot(stclContext, this, Slot.ID);
		new NumberSlot(stclContext);
		new MaxSlot(stclContext);
		new MaxNumberSlot(stclContext);
		new HitsSlot(stclContext);

		new SessionsSlot(stclContext);
		new SlotOrderSlot(stclContext, this);
		new SlotLimitSlot(stclContext, this);

		// TODO new ThreadSlot()
	}

	/**
	 * Accepts all slot name (so user can store any stencil anywhere).
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdo.stencils.Stencil#hasLocalSlot(com.gdo.stencils.StencilContext,
	 * java.lang.String)
	 */
	@Override
	protected boolean hasLocalSlot(StclContext context, String slotName) {
		return true;
	}

	/**
	 * Accepts all slot name (so user can store any stencil anywhere).
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdo.stencils.Stencil#getLocalSlot(com.gdo.stencils.StencilContext,
	 * java.lang.String, com.gdo.stencils.plug.PStencil)
	 */
	@Override
	protected PSlot<StclContext, PStcl> getLocalSlot(StclContext stclContext, String slotName, PStcl self) {

		// verifies slot name
		if (StringUtils.isBlank(slotName)) {
			throw new WrongPathException("No path defined for local slot in session stencil", this);
		}
		if (PathUtils.isComposed(slotName)) {
			String msg = String.format("Wrong composed slot name %s for local slot in session stencil", slotName);
			throw new WrongPathException(msg, this);
		}

		// returns the slot if already exists
		_Slot<StclContext, PStcl> slot = getSlots().get(slotName);
		if (slot != null) {
			return new PSlot<StclContext, PStcl>(slot, self);
		}

		// normalizes the slot name
		String name = slotName;
		if (name.endsWith(PathUtils.SEP_STR)) {
			name = StringHelper.substringEnd(name, PathUtils.SEP_INT);
		}

		// creates the slot dynamically
		MultiSlot<StclContext, PStcl> created = new MultiSlot<StclContext, PStcl>(stclContext, this, name);
		created.setVerifyUnique(true);
		created.setForceUnique(true);
		return new PSlot<StclContext, PStcl>(created, self);
	}

	/**
	 * ID slot
	 */
	private class IdSlot extends CalculatedStringPropertySlot<StclContext, PStcl> {
		public IdSlot(StclContext stclContext, Stcl in, String name) {
			super(stclContext, in, name);
		}

		@Override
		public String getValue(StclContext stclContext, PStcl self) {
			return stclContext.getHttpSession().getId();
		}
	}

	/**
	 * Number slot
	 */
	private class NumberSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {
		public NumberSlot(StclContext stclContext) {
			super(stclContext, SessionStcl.this, Slot.NUMBER);
		}

		@Override
		public int getIntegerValue(StclContext stclContext, PStcl self) {
			return NUMBER;
		}
	}

	/**
	 * Max slot
	 */
	private class MaxSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {
		public MaxSlot(StclContext stclContext) {
			super(stclContext, SessionStcl.this, Slot.MAX);
		}

		@Override
		public int getIntegerValue(StclContext stclContext, PStcl self) {
			return MAX;
		}
	}

	/**
	 * Max number slot
	 */
	private class MaxNumberSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {
		public MaxNumberSlot(StclContext stclContext) {
			super(stclContext, SessionStcl.this, Slot.MAX_NUMBER);
		}

		@Override
		public int getIntegerValue(StclContext stclContext, PStcl self) {
			return MAX_NUMBER;
		}
	}

	/**
	 * Hits slot
	 */
	private class HitsSlot extends CalculatedIntegerPropertySlot<StclContext, PStcl> {
		public HitsSlot(StclContext stclContext) {
			super(stclContext, SessionStcl.this, Slot.HITS);
		}

		@Override
		public int getIntegerValue(StclContext stclContext, PStcl self) {
			return RpcWrapper.HITS;
		}
	}

	/**
	 * Sessions slot
	 */
	private class SessionsSlot extends MultiCalculatedSlot<StclContext, PStcl> {

		public SessionsSlot(StclContext stclContext) {
			super(stclContext, SessionStcl.this, Slot.SESSIONS, PSlot.ANY);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {
			List<PStcl> res = new ArrayList<PStcl>();
			PStcl container = nullPStencil(stclContext, Result.error(""));

			for (Entry<String, Stcl> entry : SESSION_STENCILS.entrySet()) {
				Key<String> key = new Key<String>(entry.getKey());
				Stcl stcl = entry.getValue();
				StencilFactory<StclContext, PStcl> factory = (StencilFactory<StclContext, PStcl>) stclContext.getStencilFactory();
				PStcl pstcl = factory.createPStencil(stclContext, self, key, stcl.self(stclContext, container));
				if (cond == null || cond.verify(stclContext, pstcl)) {
					res.add(pstcl);
				}
			}

			// return the new list
			return new ListIterator<StclContext, PStcl>(res);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpSessionBindingListener#valueBound(javax.servlet.
	 * http.HttpSessionBindingEvent)
	 */
	@Override
	public void valueBound(HttpSessionBindingEvent event) {

		// traces session creation
		HttpSession session = event.getSession();

		ServletContext context = session.getServletContext();
		logWarn(StclContext.defaultContext(), "Session %s created (id:%s)", context.getServletContextName(), session.getId());

		// handler on after session created
		// PStcl servletStcl = (PStcl)
		// context.getAttribute(ServletStcl.class.getName());
		// ((ServletStcl)
		// servletStcl.getReleasedStencil(this)).afterSessionCreated(this,
		// servletStcl);

		// increments counters
		Stcl stcl = (Stcl) event.getValue();
		SESSION_STENCILS.put(session.getId(), stcl);
		HTTP_SESSIONS.put(session.getId(), session);
		MAX_NUMBER++;
		NUMBER++;
		if (NUMBER > MAX) {
			MAX = NUMBER;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet
	 * .http.HttpSessionBindingEvent)
	 */
	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {

		// traces session destruction
		HttpSession session = event.getSession();
		ServletContext context = session.getServletContext();
		logWarn(StclContext.defaultContext(), "Session %s destroyed (id:%s)", context.getServletContextName(), session.getId());

		// handler on before session deleted
		// ((ServletStcl)
		// servletStcl.getReleasedStencil(this)).afterSessionCreated(this,
		// servletStcl);

		// decrements counter
		SESSION_STENCILS.remove(session.getId());
		HTTP_SESSIONS.remove(session.getId());
		NUMBER--;
	}

	//
	// LOG PART
	//

	private static final StencilLog LOG = new StencilLog(SessionStcl.class);

	public static String logWarn(String format, Object... params) {
		if (LOG.isWarnEnabled()) {
			String msg = (params.length == 0) ? format : String.format(format, params);
			LOG.warn(StclContext.defaultContext(), msg);
			return msg;
		}
		return "";
	}

	public static String logError(String format, Object... params) {
		if (LOG.isErrorEnabled()) {
			String msg = (params.length == 0) ? format : String.format(format, params);
			LOG.error(StclContext.defaultContext(), msg);
			return msg;
		}
		return "";
	}

}
