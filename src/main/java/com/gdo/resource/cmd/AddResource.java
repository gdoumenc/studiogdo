/*
 * Copyright GDO - 2004
 */
package com.gdo.resource.cmd;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;

import com.gdo.context.model.FolderStcl;
import com.gdo.file.cmd.CreateFile;
import com.gdo.project.model.ComposedActionStcl;
import com.gdo.resource.model.FileResourceStcl;
import com.gdo.resource.model.ResourcesMgrStcl;
import com.gdo.resource.model._ResourceStcl;
import com.gdo.stencils.Result;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.cmd.CommandStatus;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.StencilUtils;

public class AddResource extends ComposedActionStcl {

	public interface Slot extends ComposedActionStcl.Slot {
		String CONTEXTS = "Contexts";
		String SELECTED_CONTEXT_PATH = "SelectedContextPath";
		String SELECTED_CONTEXT = "SelectedContext";
		String CREATION_MODE = "CreationMode";
		String FILE_NAME = "FileName";
		String RESOURCE_CREATED = "ResourceCreated";
	}

	public interface Status {
		int NO_RESSOURCE_CLASS_NAME = 1;
		int NO_CONTEXT_SELECTED = 2;
		int FILE_NOT_FOUND = 3;
		int WRONG_CREATION_MODE = 4;
	}

	private static final String NEW_MODE = "new";
	private static final String GET_MODE = "get";

	private String _creation_mode; // values are new, get
	private String _class_name; // resource template class name
	private boolean _name_encoding; // the name should be encoded for being read
	// from http
	private PStcl _file; // associated file
	private PStcl _resource; // resource created

	public AddResource(StclContext stclContext) {
		super(stclContext);
	}

	@Override
	public CommandStatus<StclContext, PStcl> performSteps(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		int currentStep = getActiveStepIndex();

		// if we have only one context in command then move to next step or
		// interface choose one.
		// TODO should be done in validation to force going to step2...
		if (currentStep == 1) {
			int size = self.getStencils(stclContext, Slot.CONTEXTS).size();
			if (size == 1) {
				self.setString(stclContext, Slot.SELECTED_CONTEXT_PATH, Slot.CONTEXTS);
				self.call(stclContext, ComposedActionStcl.Command.NEXT_STEP);
			}
		}

		// creates or gets the associated file
		else if (currentStep == 2) {
			PStcl context = self.getStencil(stclContext, Slot.SELECTED_CONTEXT);

			// creates the associated file
			if (NEW_MODE.equals(this._creation_mode)) {

				// defines a random name
				String fileName = "_" + System.currentTimeMillis();

				// should add folder case here (create file should be called on
				// folder)

				// create the file
				CommandStatus<StclContext, PStcl> status = context.call(stclContext, FolderStcl.Command.CREATE_FILE, fileName);
				if (status.isNotSuccess())
					return status;
				this._file = status.getInfo(CommandStatus.SUCCESS, FolderStcl.Command.CREATE_FILE, CreateFile.Status.FILE);
				if (StencilUtils.isNull(this._file))
					return error(cmdContext, self, 0, "cannot get created file", status);
			}

			// in get mode user select the file to be associated to the resource
			else if (GET_MODE.equals(this._creation_mode)) {
			} else {
			}
		}

		// creates the resource
		else if (currentStep == 3) {
			// PStcl context = self.getStencil(stclContext, Slot.SELECTED_CONTEXT);
			PStcl container = cmdContext.getTarget();

			// creates the resource
			this._resource = self.newPStencil(stclContext, Slot.RESOURCE_CREATED, Key.NO_KEY, this._class_name);
			if (StencilUtils.isNull(this._resource)) {
				String msg = String.format("cannot create resource %s in container %s", this._class_name, container);
				return error(cmdContext, self, msg);
			}

			// plugs resource in temporary slot for final modification before
			// adding it to the resources
			// PStcl stcl = this._resource.plug(stclContext, context,
			// _ResourceStcl.Slot.CONTEXT);
			// if (StencilUtils.isNull(stcl)) return error(cmdContext, self,
			// "cannot plug contel time:!!!xt in created resource");
			PStcl stcl = this._resource.plug(stclContext, this._file, _ResourceStcl.Slot.FILE);
			if (StencilUtils.isNull(stcl))
				return error(cmdContext, self, "cannot plug created file in resource");

			// replaces container folder and manager
			this._resource.clearSlot(stclContext, _ResourceStcl.Slot.CONTAINER_FOLDER);
			this._resource.plug(stclContext, container, _ResourceStcl.Slot.CONTAINER_FOLDER);
			PStcl mgr = container.getStencil(stclContext, _ResourceStcl.Slot.CONTAINER_MANAGER);
			this._resource.clearSlot(stclContext, _ResourceStcl.Slot.CONTAINER_MANAGER);
			this._resource.plug(stclContext, mgr, _ResourceStcl.Slot.CONTAINER_MANAGER);
		}

		// validates the resource creation
		else if (currentStep == 4) {
			PStcl container = cmdContext.getTarget();

			// plugs the created resource in container
			PStcl resource = container.plug(stclContext, this._resource, ResourcesMgrStcl.Slot.FILE_RESOURCES);
			if (StencilUtils.isNull(resource)) {
				String msg = String.format("cannot plug created resource %s in container %s", this._resource, container);
				return error(cmdContext, self, msg);
			}
		}

		return success(cmdContext, self);
	}

	@Override
	public CommandStatus<StclContext, PStcl> cancel(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {

		// remove created file if in creation mode
		if (NEW_MODE.equals(this._creation_mode)) {
			if (StencilUtils.isNotNull(this._file)) {
				// to be done;
			}
		}

		return super.cancel(cmdContext, self);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> verifyContext(CommandContext<StclContext, PStcl> cmdContext, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		int activeStep = getActiveStepIndex();

		// checks initial parameters
		if (activeStep == 1) {
			this._creation_mode = getParameter(cmdContext, 1, NEW_MODE);
			if (!NEW_MODE.equals(this._creation_mode) && !GET_MODE.equals(this._creation_mode)) {
				String msg = String.format("Creation mode %s should be 'new' or 'get' (param1)", this._creation_mode);
				return error(cmdContext, self, Status.WRONG_CREATION_MODE, msg);
			}
			self.setString(stclContext, Slot.CREATION_MODE, this._creation_mode);

			this._class_name = getParameter(cmdContext, 2, FileResourceStcl.class.getName());
			this._name_encoding = getParameter(cmdContext, 3, false);
		}

		return super.verifyContext(cmdContext, self);
	}

	@Override
	protected CommandStatus<StclContext, PStcl> beforeIncrementStep(CommandContext<StclContext, PStcl> cmdContext, int increment, PStcl self) {
		StclContext stclContext = cmdContext.getStencilContext();
		int activeStep = getActiveStepIndex();

		// a context must be defined for the resource
		if (increment != 0 && activeStep == 1) {

			// plug the selected context
			self.clearSlot(stclContext, Slot.SELECTED_CONTEXT);
			PStcl context = self.getStencil(stclContext, self.getString(stclContext, Slot.SELECTED_CONTEXT_PATH, ""));
			if (StencilUtils.isNull(context)) {
				return error(cmdContext, self, Status.NO_CONTEXT_SELECTED, "No context selected for the resource");
			}
			PStcl selectCtx = self.plug(stclContext, context, Slot.SELECTED_CONTEXT);
			if (StencilUtils.isNull(selectCtx))
				return error(cmdContext, self, "cannot plug Selected context in command");
		}

		return success(cmdContext, self);
	}

	@Override
	public void multipart(StclContext stclContext, String fileName, FileItem item, PStcl self) throws Exception {
		int activeStep = getActiveStepIndex();

		// we can consider the upload can be done only at step 2
		if (activeStep == 2) {
			if (StencilUtils.isNull(this._file)) {
				if (getLog().isWarnEnabled())
					getLog().warn(stclContext, "No file found for the resource to upload it");
				return;
			}
			this._file.multipart(stclContext, fileName, item);

			// Rename the file.
			String newFileName = item.getName();
			if (StringUtils.isNotEmpty(newFileName)) {
				if (this._name_encoding) {
					encodeUrl(newFileName);
				}
				Result status = this._file.call(stclContext, "Rename", newFileName);
				if (status.isNotSuccess()) {
					if (getLog().isWarnEnabled()) {
						String msg = String.format("Cannot rename to %s from multipart", newFileName);
						getLog().warn(stclContext, msg);
					}
				}
				// Should be done by renamethis._file.setString(stclContext,
				// FileStcl.Slot.NAME, fName);
			}
		}
	}

	private String encodeUrl(String url) {
		url = url.replaceAll(" ", "_");
		url = url.replaceAll("%20", "_");
		return url;
	}

}