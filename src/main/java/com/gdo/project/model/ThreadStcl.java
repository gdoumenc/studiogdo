/**
 * Copyright GDO - 2005
 */
package com.gdo.project.model;

import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.atom.Atom;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.key.IKey;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;

/**
 * <p>
 * Abstract encapulation class for a java thread.
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
public class ThreadStcl extends Stcl implements Runnable {

	public interface Slot extends Stcl.Slot {
		String COMMAND = "Command";
	}

	public interface ICommandThread extends Runnable {
		void setThread(ThreadStcl thread);
	}

	private CommandContext<StclContext, PStcl> _context; // the command context
																												// calling
	private PStcl _self; // this thread stencil as a plugged stencil
	private PStcl _reference; // the plugged stencil referenced from the thread at
														// creation
	private ICommandThread _runnable;

	// declared public only to allow factory to create it
	public ThreadStcl(StclContext stclContext, CommandContext<StclContext, PStcl> cmdContext, ICommandThread runnable) {
		super(stclContext);
		_context = cmdContext;
		_runnable = runnable;
		runnable.setThread(this);

		singleSlot(Slot.COMMAND);
	}

	public static void createThread(CommandContext<StclContext, PStcl> cmdContext, ICommandThread runnable, PStcl reference) {
		StclContext stclContext = cmdContext.getStencilContext();

		// creates the thread stencil in the session threads slots
		PStcl session = stclContext.getServletStcl().getStencil(stclContext, ServletStcl.Slot.SESSION);
		IKey key = new Key<String>(Atom.uniqueID());
		PStcl threadStcl = session.newPStencil(stclContext, SessionStcl.Slot.THREADS, key, ThreadStcl.class, cmdContext, runnable);
		if (threadStcl.isNull()) {
			reference.logWarn(stclContext, "Cannot create the thread for command %s", reference);
			return;
		}

		// starts the thread
		ThreadStcl t = (ThreadStcl) threadStcl.getReleasedStencil(stclContext);
		t._self = threadStcl;
		t._reference = reference;
		new Thread(t).start();
	}

	public final CommandContext<StclContext, PStcl> getCommandContext() {
		return _context;
	}

	public final StclContext getStencilContext() {
		return _context.getStencilContext();
	}

	@Override
	public PStcl self() {
		return _self;
	}

	public PStcl getReference() {
		return _reference;
	}

	@Override
	public void run() {
		StclContext stclContext = getStencilContext();
		logWarn(stclContext, "running thread %s", _self);

		// runs the associated thread
		_runnable.run();

		// removes the thread from the session (TODO should be done when thread
		// stops)
		IKey key = _self.getKey();
		PStcl session = stclContext.getServletStcl().getStencil(stclContext, ServletStcl.Slot.SESSION);
		session.unplugOtherStencilFrom(stclContext, PathUtils.createPath(SessionStcl.Slot.THREADS, key));
	}
}
