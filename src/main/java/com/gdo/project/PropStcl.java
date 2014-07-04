/**
 * Copyright GDO - 2004
 */
package com.gdo.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.gdo.helper.StringHelper;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.prop.MemoryPropStcl;
import com.gdo.stencils.util.PathUtils;

/**
 * <p>
 * Basic implementation of the studiogdo property stencil.
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
 *         href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com)</a>
 */
public class PropStcl extends MemoryPropStcl {

	private String _tmpFileName; // file used to store content if size is too

	// big

	public PropStcl(StclContext stclContext) {
		this(stclContext, (String) null);
	}

	public PropStcl(StclContext stclContext, String value) {
		super(stclContext, value);

		// stores in temporary file if value length too big
		if (value != null && value.length() >= MAX_VALUE_SIZE) {
			saveValueInTemporaryFile(stclContext, value.toString());
		}
	}

	public PropStcl(StclContext stclContext, Boolean value) {
		this(stclContext, value.toString());
	}

	public PropStcl(StclContext stclContext, Integer value) {
		this(stclContext, value.toString());
	}

	@Override
	public InputStream getInputStream(StclContext stclContext, PStcl self) {

		// if the content was stored in a temporary file
		if (this._tmpFileName != null) {
			return inputStreamValueFromTemporaryFile(stclContext);
		}
		return super.getInputStream(stclContext, self);
	}

	/**
	 * @return the property value defined in the properties file if <tt>id</tt>
	 *         defined (if id defined and no property return the id)
	 */
	@Override
	public String getValue(StclContext stclContext, PStcl self) {

		// if the content was stored in a temporary file
		if (this._tmpFileName != null) {
			return readValueFromTemporaryFile(stclContext);
		}
		return super.getValue(stclContext, self);
	}

	@Override
	public String setValue(StclContext stclContext, String value, PStcl self) {
		if (value == null)
			value = "";

		// saves value content in file if value length is too big
		if (value.length() >= MAX_VALUE_SIZE) {
			return saveValueInTemporaryFile(stclContext, value);
		}

		// removes previous file if was in temporary file
		if (this._tmpFileName != null) {
			File file = new File(this._tmpFileName);
			if (!file.delete()) {
				if (getLog().isWarnEnabled()) {
					String msg = String.format("not able to remove temporary file %s", this._tmpFileName);
					getLog().warn(stclContext, msg);
				}
			}
		}

		// does as before... if not
		this._tmpFileName = null;
		return super.setValue(stclContext, value, self);
	}

	// saves value content in file
	private String saveValueInTemporaryFile(StclContext stclContext, String value) {
		try {

			// create property file name
			String home = stclContext.getConfigParameter(StclContext.PROJECT_TMP_DIR);
			String name = String.format("prop%s.txt", getUId(stclContext));
			this._tmpFileName = PathUtils.compose(home, name);

			// save in file
			File file = new File(this._tmpFileName);
			file.createNewFile();
			FileWriter out = new FileWriter(file);
			IOUtils.write(value, out);
			out.close();

			// free memory
			this._value = null;
		} catch (IOException e) {
			logError(stclContext, e.toString());
			this._tmpFileName = null;
			this._value = value;
		}
		return value;
	}

	// saves value content in file
	private InputStream inputStreamValueFromTemporaryFile(StclContext stclContext) {
		try {
			File file = new File(this._tmpFileName);
			return new FileInputStream(file);
		} catch (Exception e) {
			logError(stclContext, e.toString());
			return StringHelper.EMPTY_STRING_INPUT_STREAM;
		}
	}

	private String readValueFromTemporaryFile(StclContext stclContext) {
		try {
			FileReader in = null;
			try {
				File file = new File(this._tmpFileName);
				in = new FileReader(file);
				return IOUtils.toString(in);
			} catch (Exception e) {
				logError(stclContext, e.toString());
				return e.getMessage();
			} finally {
				if (in != null)
					in.close();
			}
		} catch (Exception e) {
			logError(stclContext, e.toString());
			return e.getMessage();
		}
	}
}