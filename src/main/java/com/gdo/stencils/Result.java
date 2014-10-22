/*
 * Copyright GDO - 2005
 */
package com.gdo.stencils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.StringHelper;
import com.google.gson.Gson;

/**
 * <p>
 * A <tt>Result</tt> is a common function call result.
 * </p>
 * <p>
 * It contains a result status (gravity level) and a complementary info if
 * needed.
 * </p>
 * <p>
 * A result may not be unique, it can be connected to another ones (cascading
 * results). The status of a composed result is defined by its max level of
 * composing results status.
 * </p>
 * <p>
 * A result information is made of :
 * <ul>
 * <li>a prefix (for unique indexing),
 * <li>an index to fetch the information,
 * <li>the default description
 * </ul>
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
public class Result {

	// gravity levels
	public static final byte SUCCESS = 0;
	public static final byte WARNING = 1;
	public static final byte ERROR = 2;

	// default information index
	public static final byte DEFAULT_INDEX = 0;

	/**
	 * Status gravity level
	 */
	private byte _status;
	/**
	 * Info associated (message, exception, ..)
	 */
	private ResultInfo _info;
	/**
	 * Status may be not unique (cascading status)
	 */
	private Result _other;

	//
	// STATIC CONSTRUCTORS
	//

	// success
	public static Result success() {
		return success("", DEFAULT_INDEX, null, null);
	}

	public static Result success(Object value) {
		return success("", DEFAULT_INDEX, value, null);
	}

	public static Result success(String prefix, Object value) {
		return success(prefix, DEFAULT_INDEX, value, null);
	}

	public static Result success(String prefix, int id, Object value) {
		return success(prefix, id, value, null);
	}

	public static Result success(String prefix, int id, Object value, Result other) {
		return new Result(Result.SUCCESS, prefix, id, value, other);
	}

	// warning
	public static Result warn(Object value) {
		return warn("", DEFAULT_INDEX, value, null);
	}

	public static Result warn(String prefix, Object value) {
		return warn(prefix, DEFAULT_INDEX, value, null);
	}

	public static Result warn(String prefix, int id, Object value) {
		return warn(prefix, id, value, null);
	}

	public static Result warn(String prefix, int id, Object value, Result other) {
		return new Result(Result.WARNING, prefix, id, value, other);
	}

	// error
	public static Result error(Object value) {
		return error("", DEFAULT_INDEX, value, null);
	}

	public static Result error(String prefix, Object value) {
		return error(prefix, DEFAULT_INDEX, value, null);
	}

	public static Result error(String prefix, int id, Object value) {
		return error(prefix, id, value, null);
	}

	public static Result error(String prefix, int id, Object value, Result other) {
		return new Result(Result.ERROR, prefix, id, value, other);
	}

	/**
	 * This constructor may not be used directly (use static functions).
	 * 
	 * @param status
	 *          the result status (SUCCESS, WARNING or ERROR).
	 * @param prefix
	 * @param index
	 * @param value
	 * @param other
	 */
	protected Result(byte status, String prefix, int index, Object value, Result other) {

		// checks status parameter
		if (status < SUCCESS || status > ERROR) {
			throw new IllegalArgumentException("illegal result status");
		}

		// checks other value to avoid circular issue (not complete but at least
		// one step circular error founded)
		if (_other == this) {
			throw new IllegalArgumentException("cannot complete same result in result");
		}

		// constructs structure
		_status = status;
		if (value != null) {
			_info = new ResultInfo(prefix, index, value);
		}
		_other = other;
	}

	/**
	 * Adds another result in the cascading list of results.
	 * 
	 * @param other
	 *          Result to be added to the list of cascading results.
	 * @return The other result added.
	 */
	public final void addOther(Result other) {
		if (_other == null) {
			_other = other;
		} else {
		    
		    // avoid loop
		    if (this != other)
		        _other.addOther(other);
		}
	}

	/**
	 * Gets the maximum gravity level found in all cascading list of results.
	 * 
	 * @return The status, i.e. the max gravity level found.
	 */
	public final byte getStatus() {

		// gets this level
		byte status = _status;

		// changes by other one if higher
		if (_other != null) {
			byte s = _other.getStatus();
			if (s > status) {
				status = s;
			}
		}
		return status;
	}

	/**
	 * Gets the list of informations found in the cascading list of results. The
	 * keys of the map are the result levels.
	 * 
	 * @return A map of informations list indexed by status.
	 */
	public final Map<Byte, List<ResultInfo>> getInfos() {
		Byte status = new Byte(_status);
		Map<Byte, List<ResultInfo>> map;

		// gets the map
		if (_other == null) {
			map = new ConcurrentHashMap<Byte, List<ResultInfo>>();
		} else {
			map = _other.getInfos();
		}

		// adds the info to the list if defined
		if (_info != null) {
			List<ResultInfo> list = (map.containsKey(status)) ? map.get(status) : new ArrayList<ResultInfo>();
			list.add(_info);
			map.put(status, list);
		}

		return map;
	}

	/**
	 * Gets the list of informations found in the cascading list of results for a
	 * specific gravity level.
	 * 
	 * @param status
	 *          Status (gravity level) expected.
	 * @return The list of informations associated to the status.
	 */
	public final List<ResultInfo> getInfos(byte status) {
		List<ResultInfo> list;

		// gets list of informations for this status
		if (_other == null) {
			list = new ArrayList<ResultInfo>();
		} else {
			list = _other.getInfos(status);
		}

		// adds information if same status
		if (status == _status && _info != null) {
			list.add(_info);
		}

		return list;
	}

	/**
	 * Returns the default value associated with no prefix to a success result.
	 * 
	 * @return the value associated to the result, <tt>null</tt> if not found.
	 */
	public final <T> T getSuccessValue() {
		return this.<T> getInfo(SUCCESS, "", 0);
	}

	/**
	 * Returns the value associated to a success result depending on a prefix.
	 * 
	 * @param prefix
	 *          the prefix to retrieve the good value.
	 * @return the value associated to a result, <tt>null</tt> if not found.
	 */
	public final <T> T getSuccessValue(String prefix) {
		return this.getInfo(SUCCESS, prefix, 0);
	}

	/**
	 * Returns the first default value associated to an error result.
	 * 
	 * @return the first value associated to the result, <tt>null</tt> if not
	 *         found.
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getErrorValue() {
		for (ResultInfo info : getInfos(ERROR)) {
			return (T) info.getValue();
		}
		return null;
	}

	/**
	 * Returns the value associated depending on a level result, a prefix and in
	 * index.
	 * 
	 * @param level
	 *          the level required.
	 * @param prefix
	 *          the prefix to retrieve the good value.
	 * @param index
	 *          the inedx to retrieve the good value.
	 * @return the value associated to a result, <tt>null</tt> if not found.
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getInfo(byte level, String prefix, int index) {
		for (ResultInfo r : getInfos(level)) {

			// checks only on same level result
			if (index == r.getIndex()) {

				// if search on prefix only
				if (StringUtils.isNotBlank(prefix)) {
					if (prefix.equals(r.getPrefix())) {
						return (T) r.getValue();
					} else {
						continue;
					}
				} else

				// returns first reason found
				{
					return (T) r.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * @param level
	 * @param prefix
	 * @param index
	 * @param value
	 */
	public final <T> void setInfo(byte level, String prefix, int index, T value) {
		for (ResultInfo r : getInfos(level)) {

			// checks only on same level result
			if (index == r.getIndex()) {

				// if search on prefix only
				if (StringUtils.isNotBlank(prefix)) {
					if (prefix.equals(r.getPrefix())) {
						r.setValue(value);
						return;
					} else {
						continue;
					}
				} else

				// returns first reason found
				{
					r.setValue(value);
					return;
				}
			}
		}
	}

	/**
	 * Checks if the result is a success.
	 * 
	 * @return <tt>true</tt> if all status are <tt>OK</tt> or <tt>WARNING</tt>.
	 */
	public final boolean isSuccess() {
		if (_status == ERROR) {
			return false;
		}
		if (_other == null) {
			return true;
		}
		return _other.isSuccess();
	}

	/**
	 * Checks if the result is not a success.
	 * 
	 * @return <tt>true</tt> if at least one status is <tt>ERROR</tt>.
	 */
	public final boolean isNotSuccess() {
		return (!isSuccess());
	}

	/**
	 * @return a JSON array containing all infos.
	 */
	public String jsonValue(byte status) {
		JSONResult result = new JSONResult();
		Gson gson = new Gson();
		result.result = getStatus();
		List<ResultInfo> list = getInfos(status);
		Iterator<ResultInfo> iter = list.iterator();
		while (iter.hasNext()) {
			ResultInfo info = iter.next();
			result.infos.add(info.getValue().toString());
		}
		return gson.toJson(result);
	}

	@SuppressWarnings("unused")
	private class JSONResult {
		byte result;
		Vector<String> infos = new Vector<String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		int status = getStatus();
		if (status == SUCCESS) {
			return "ok : ";
		} else if (status == WARNING) {
			return "warning" + getMessage();
		} else if (status == ERROR) {
			return "error" + getMessage();
		}
		return "unknown level";
	}

	/**
	 * Returns a human readable message for this result.
	 * 
	 * @return the message.
	 */
	public String getMessage() {
		StringBuffer buffer = new StringBuffer();
		getMessage(buffer);
		return buffer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdo.stencils.Result#getMessage()
	 */
	private StringBuffer getMessage(StringBuffer buffer) {

		// adds info value on error
		if (_info != null) {
			Object value = _info.getValue();
			if (value != null) {
				buffer.append(value.toString());
			}
		}

		// adds other error info
		if (_other != null) {
			buffer.append(" - ");
			_other.getMessage(buffer);
		}

		return buffer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Byte) {
			return ((Byte) obj).byteValue() == getStatus();
		}
		if (obj instanceof Result) {
			return ((Result) obj).getStatus() == getStatus();
		}
		return false;
	}

	/**
	 * <p>
	 * A <tt>ResultInfo</tt> is made of :
	 * <ul>
	 * <li>a prefix (usually class name),
	 * <li>an index,
	 * <li>the associated value
	 * </ul>
	 * </p>
	 */
	public class ResultInfo {
		private String _prefix;
		private int _index;
		private Object _value;

		public ResultInfo(String prefix, int index, Object value) {
			_prefix = (prefix != null) ? prefix : StringHelper.EMPTY_STRING;
			_index = index;
			_value = value;
		}

		/**
		 * Returns the prefix of this result info.
		 * 
		 * @return the prefix of this result info.
		 */
		public String getPrefix() {
			return _prefix;
		}

		/**
		 * Returns the index of this result info.
		 * 
		 * @return the index of this result info.
		 */
		public int getIndex() {
			return _index;
		}

		/**
		 * Returns the value of this result info.
		 * 
		 * @return the value of this result info.
		 */
		public Object getValue() {
			return _value;
		}

		public void setValue(Object value) {
			_value = value;
		}
	}

}
