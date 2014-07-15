/**
 * Copyright GDO - 2005
 */
package com.gdo.stencils.interpreted;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.gdo.stencils._Stencil;
import com.gdo.stencils._StencilContext;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.IStencilFactory.Mode;
import com.gdo.stencils.factory.StencilFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.log.StencilLog;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug._PStencil;
import com.gdo.stencils.prop.PropStencil;
import com.gdo.stencils.slot.Annotation;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;
import com.gdo.util.XmlWriter;

/**
 * <p>
 * Repository of all instances needed for loading/creating a stencil.
 * <p>
 * The instances may be created and add to the repository if they don't exist
 * and if there is a descriptor associated. The instances may be created on
 * demand, for lazy evaluation. A specific slot is defined for PARENT path (..)
 * manipulation. <blockquote>
 * <p>
 * &copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved. This
 * software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.
 * </p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com)</a>
 * @see com.gdo.stencils.cmd.CommandContext Context
 */
public final class InstanceRepository<C extends _StencilContext, S extends _PStencil<C, S>> {
	private static final StencilLog _log = new StencilLog(InstanceRepository.class);

	private final Map<String, S> _instances = new HashMap<String, S>(); // stencils
	// already
	// created
	// (not
	// plugged)
	// private final Map<String, String> _refs = new HashMap<String, String>();
	// // references modified (if doesn't starts with _ or created then the uid
	// is changed)
	private final Map<String, InstDescriptor<C, S>> _descriptors = new HashMap<String, InstDescriptor<C, S>>(); // stencil
	// descriptors
	// list
	private final Stack<String> _pwd = new Stack<String>(); // current path for
	// searching a
	// stencil (always
	// ends with '/')
	private Mode _mode; // mode for all stencils in the repository (creation
	// status for this repository)
	private String _root_id; // initial root id

	public InstanceRepository() {
		this._mode = Mode.ON_CREATION;
		this._pwd.push(PathUtils.ROOT);
		this._root_id = ""; // on creation the root refers to /
	}

	public InstanceRepository(String rootId) {
		this._mode = Mode.ON_LOAD;
		this._pwd.push(PathUtils.ROOT); // on load the root is the stencil id
		this._root_id = PathUtils.ROOT + rootId; // on load the root refers to
		// the stencil id (/stencil_id)
	}

	public Mode getMode() {
		return this._mode;
	}

	public void setMode(Mode mode) {
		this._mode = mode;
	}

	/**
	 * @return <tt>true</tt> is the repository is empty.
	 */
	public boolean isEmpty() {
		if (this._instances != null && this._instances.size() > 0)
			return false;
		if (this._descriptors != null && this._descriptors.size() > 0)
			return false;
		return true;

	}

	/**
	 * Store instance in a specific slot (to access ..).
	 */
	public S store(C stclContext, String name, _Stencil<C, S> inst) {
		String path = getAbsolutePath(name);
		S current = this._instances.get(this._pwd.peek());
		StencilFactory<C, S> factory = (StencilFactory<C, S>) stclContext.<C, S> getStencilFactory();
		S stored = null;
		if (inst instanceof PropStencil) {
			stored = factory.newPPropStencil(stclContext, new InstanceSlot(current), Key.NO_KEY, inst);
		} else {
			stored = factory.newPStencil(stclContext, new InstanceSlot(current), Key.NO_KEY, inst);
		}
		this._instances.put(path, stored);
		return stored;
	}

	/**
	 * Store instance as descriptor for lazy evaluation.
	 */
	public String store(InstDescriptor<C, S> instDesc) {
		String path = getAbsolutePath(instDesc.getId());
		if (!this._descriptors.containsKey(path)) {
			this._descriptors.put(path, instDesc);
		} else {
			if (getLog().isWarnEnabled()) {
				// TODO getLog().warn(null,
				// String.format("Instance %s declared twice at path %s",
				// instDesc, path));
			}
		}
		return path;
	}

	/*
	 * public boolean refHasChanged(C stclContext, String name) { String path =
	 * getAbsolutePath(name); if (this._instances.containsKey(path)) return true;
	 * String ref = PathUtils.getLastName(path.substring(0, path.length() - 1));
	 * if (!ref.startsWith("_")) { if (!this._refs.containsKey(path))
	 * this._refs.put(path, PathUtils.SEP_STR + new PStencil<C, S>(null, null,
	 * null).getUID()); return true; } return false; } public String getNewRef(C
	 * stclContext, String name) { String path = getAbsolutePath(name); if
	 * (this._instances.containsKey(path)) return PathUtils.SEP_STR +
	 * this._instances.get(path).getUID(); return this._refs.get(path); }
	 */

	/**
	 * @return the stencil defined at reference.
	 */
	public S getInstance(C stclContext, String ref) {
		String path = getAbsolutePath(ref);

		// not already created, creates stencil from descriptor
		if (!this._instances.containsKey(path)) {
			InstDescriptor<C, S> instanceDesc = this._descriptors.get(path);
			if (instanceDesc == null) {
				if (getLog().isErrorEnabled()) {
					String msg = String.format("Undefined instance description for ref %s", path);
					getLog().error(stclContext, msg);
				}
				return null;
			}
			instanceDesc.createInstance(stclContext, ref, this, 0); // instance
			// will be
			// stored in
			// the
			// repository
			// by
			// creation
		}

		// verify the stencil
		S stcl = this._instances.get(path);
		if (StencilUtils.isNull(stcl) && getLog().isErrorEnabled()) {
			String msg = String.format("Was not able to create instance ref %s", path);
			getLog().error(stclContext, msg);
		}
		return stcl;
	}

	public boolean saveInstance(C stclContext, String name, XmlWriter writer) {
		/*
		 * String path = getAbsolutePath(name); // search already created stencil if
		 * (this._instances.containsKey(path)) { Stencil<C, S> stcl = ((PStencil<C,
		 * S>) this._instances.get(path)).getStencil();
		 * stcl.saveAsInstance(stclContext, writer, this); return true; } // save
		 * instance descriptor if (this._descriptors.containsKey(path)) {
		 * InstDescriptor<C, S> instanceDesc = this._descriptors.get(path);
		 * instanceDesc.save(stclContext, writer, this); return true; }
		 */
		return false;
	}

	public void push(String name) {
		this._pwd.push(getAbsolutePath(name));
	}

	public void pop() {
		this._pwd.pop();
	}

	/**
	 * @return the absolute path of the instance.
	 */
	public String getAbsolutePath(String path) {
		return getAbsolutePathFromPwd(this._pwd.peek(), completePath(path));
	}

	/**
	 * @return the absolute path for a path from the current pwd.
	 */
	private String getAbsolutePathFromPwd(String pwd, String path) {

		// dont need current pwd is path is absolute (initial root added only)
		if (path.startsWith(PathUtils.ROOT)) {
			String rootId = this._root_id;
			return rootId + completePath(path);
		}

		int index = PathUtils.indexOf(path, PathUtils.SEP_STR);
		if (index == -1) {
			if (PathUtils.PARENT.equals(path)) {
				return completePath(PathUtils.getPathName(pwd));
			}
			if (PathUtils.THIS.equals(path)) {
				return pwd.substring(0, pwd.length() - 1);
			}
			return completePath(pwd + path);
		}

		String first = PathUtils.getFirstName(path);
		if (PathUtils.PARENT.equals(first)) {
			String parent = completePath(PathUtils.getPathName(pwd.substring(0, pwd.length() - 1)));
			return getAbsolutePathFromPwd(parent, PathUtils.getTailName(path));
		}
		return getAbsolutePathFromPwd(pwd + first + PathUtils.SEP, PathUtils.getTailName(path));
	}

	public static StencilLog getLog() {
		return _log;
	}

	/**
	 * @return the path terminated by '/' if not.
	 */
	private String completePath(String path) {
		return (!path.endsWith(PathUtils.SEP_STR)) ? path + PathUtils.SEP_STR : path;
	}

	private class InstanceSlot extends PSlot<C, S> {

		private S _container; // the container stencil

		public InstanceSlot(S container) {
			super(null, container);
		}

		@Override
		public String getName(C stclContext) {
			return String.format("[Repository in %s]", this._container);
		}

		@Override
		public char getArity(C stclContext) {
			return PSlot.ONE;
		}

		@Override
		public int size(C stclContext, StencilCondition<C, S> cond) {
			return 1;
		}

		@Override
		public boolean hasStencils(C stclContext, StencilCondition<C, S> cond) {
			return true;
		}

		@Override
		public StencilIterator<C, S> getStencils(C stclContext, StencilCondition<C, S> cond) {
			return StencilUtils.<C, S> iterator(stclContext, this._container.self(), this._container.getContainingSlot());
		}

		@Override
		public boolean contains(C stclContext, StencilCondition<C, S> cond, S searched) {
			_Stencil<C, S> stcl = searched.getReleasedStencil(stclContext);
			return this._container.equals(stcl);
		}

		@Override
		public boolean canChangeOrder(C stclContext) {
			return false;
		}

		@Override
		public boolean isFirst(C stclContext, S searched) {
			return true;
		}

		@Override
		public boolean isLast(C stclContext, S searched) {
			return true;
		}

		@Override
		public Annotation getAnnotation(C stclContext, String type) {
			return null;
		}

		@Override
		public void setAnnotation(C stclContext, String type, Annotation annotation) {
		}

		@Override
		public S getContainer() {
			return null;
		}

		@Override
		public String toString() {
			if (this._container != null) {
				return this._container.toString();
			}
			return "";
		}

	}
}
