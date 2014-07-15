/**
 * Copyright GDO - 2004
 */
package com.gdo.project.util.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils.Result;
import com.gdo.stencils.Stcl;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cond.PathCondition;
import com.gdo.stencils.cond.StencilCondition;
import com.gdo.stencils.factory.StclFactory;
import com.gdo.stencils.iterator.StencilIterator;
import com.gdo.stencils.key.Key;
import com.gdo.stencils.plug.PSlot;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.slot.MultiCalculatedSlot;
import com.gdo.stencils.util.PathUtils;
import com.gdo.stencils.util.StencilUtils;

public class DateStcl extends Stcl {

	public interface Slot extends Stcl.Slot {
		String NOW = "Now";
		String DATE_FORMAT = "DateFormat";

		String YEAR = "Year";
		String MONTH = "Month";
		String DAY = "Day";
		String DAY_OF_WEEK = "DayOfWeek";
		String DAY_OF_YEAR = "DayOfYear";
		String HOUR = "Hour";

		String MONTH_NAME = "MonthName";
		String DAY_NAME = "DayName";
	}

	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
	public static final String DEFAULT_SQL_DATE_FORMAT = "yyyy-MM-dd";
	public static final String FR_DATE_FORMAT = "dd/MM/yyyy";

	public DateStcl(StclContext stclContext) {
		super(stclContext);

		new NowSlot(stclContext);
		new DateFormatSlot(stclContext);
		new YearSlot(stclContext);
		new MonthSlot(stclContext);
		new DaySlot(stclContext);
		new DayOfWeekSlot(stclContext);
		new DayOfYearSlot(stclContext);
		new HourSlot(stclContext);
		new MonthNameSlot(stclContext);
		new DayNameSlot(stclContext);
	}

	private class NowSlot extends MultiCalculatedSlot<StclContext, PStcl> {
		public NowSlot(StclContext stclContext) {
			super(stclContext, DateStcl.this, Slot.NOW, PSlot.ANY);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

			// gets the date format
			String date;
			String format = PathCondition.getKeyCondition(cond);
			if (StringUtils.isEmpty(format)) {
				date = Long.toString(new Date().getTime());
			} else {
				DateFormat dateFormat = new SimpleDateFormat(format);
				date = dateFormat.format(new Date());
			}

			// creates the property
			StclFactory factory = (StclFactory) stclContext.getStencilFactory();
			PStcl prop = factory.createPProperty(stclContext, null, Key.NO_KEY, date);
			return StencilUtils.< StclContext, PStcl> iterator(stclContext, prop, self);
		}
	}

	private class DateFormatSlot extends MultiCalculatedSlot<StclContext, PStcl> {
		public DateFormatSlot(StclContext stclContext) {
			super(stclContext, DateStcl.this, Slot.DATE_FORMAT, PSlot.ANY);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

			// the condition must be a key condition
			String key = PathCondition.getKeyCondition(cond);
			if (StringUtils.isEmpty(key)) {
				String msg = String.format("A key path condition should be defined for slot %s (not %s)", getName(stclContext), key);
				return StencilUtils.< StclContext, PStcl> iterator(Result.error(msg));
			}

			// gets time
			String[] str = StringUtils.split(key, PathUtils.MULTI);
			long time = 0;
			try {
				time = Long.parseLong(str[0]);
			} catch (Exception e) {
				String msg = String.format("The time should be defined as a long (not %s)", time);
				return StencilUtils.< StclContext, PStcl> iterator(Result.error(msg));
			}

			// creates date format
			String format = (str.length == 1) ? DEFAULT_DATE_FORMAT : str[1];
			DateFormat dateFormat = new SimpleDateFormat(format);
			String formatted = dateFormat.format(new Date(time));

			// creates the property
			StclFactory factory = (StclFactory) stclContext.getStencilFactory();
			PStcl prop = factory.createPProperty(stclContext, null, Key.NO_KEY, formatted);
			return StencilUtils.< StclContext, PStcl> iterator(stclContext, prop, self);
		}
	}

	private class YearSlot extends CalendarSlot {
		public YearSlot(StclContext stclContext) {
			super(stclContext, Slot.YEAR, Calendar.YEAR);
		}
	}

	private class MonthSlot extends CalendarSlot {
		public MonthSlot(StclContext stclContext) {
			super(stclContext, Slot.MONTH, Calendar.MONTH);
		}
	}

	private class DaySlot extends CalendarSlot {
		public DaySlot(StclContext stclContext) {
			super(stclContext, Slot.DAY, Calendar.DAY_OF_MONTH);
		}
	}

	private class DayOfWeekSlot extends CalendarSlot {
		public DayOfWeekSlot(StclContext stclContext) {
			super(stclContext, Slot.DAY_OF_WEEK, Calendar.DAY_OF_WEEK);
		}
	}

	private class DayOfYearSlot extends CalendarSlot {
		public DayOfYearSlot(StclContext stclContext) {
			super(stclContext, Slot.DAY_OF_YEAR, Calendar.DAY_OF_YEAR);
		}
	}

	private class HourSlot extends CalendarSlot {
		public HourSlot(StclContext stclContext) {
			super(stclContext, Slot.HOUR, Calendar.HOUR);
		}
	}

	private class MonthNameSlot extends MultiCalculatedSlot<StclContext, PStcl> {

		public MonthNameSlot(StclContext stclContext) {
			super(stclContext, DateStcl.this, Slot.MONTH_NAME, PSlot.ANY);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

			// the condition must be a key condition
			String key = PathCondition.getKeyCondition(cond);
			if (StringUtils.isEmpty(key)) {
				String msg = String.format("A key path condition should be defined for slot %s (not %s)", getName(stclContext), key);
				return StencilUtils.< StclContext, PStcl> iterator(Result.error(msg));
			}

			// gets month index
			String[] str = StringUtils.split(key, PathUtils.MULTI);
			int month = 0;
			month = Integer.parseInt(str[0]);
			if (month < 0 || month > 11) {
				String msg = String.format("The month should be defined between 0 and 11 (not %s)", month);
				return StencilUtils.< StclContext, PStcl> iterator(Result.error(msg));
			}

			// creates date format
			String format = (str.length == 1) ? "%Tb" : str[1];
			Calendar cal = Calendar.getInstance(TimeZone.getDefault(), stclContext.getLocale());
			cal.set(0, month + 1, 0);
			String name = String.format(stclContext.getLocale(), format, cal.getTime());

			// creates the property
			StclFactory factory = (StclFactory) stclContext.getStencilFactory();
			PStcl prop = factory.createPProperty(stclContext, null, Key.NO_KEY, name);
			return StencilUtils.< StclContext, PStcl> iterator(stclContext, prop, self);
		}
	}

	private class DayNameSlot extends MultiCalculatedSlot<StclContext, PStcl> {

		public DayNameSlot(StclContext stclContext) {
			super(stclContext, DateStcl.this, Slot.DAY_NAME, PSlot.ANY);
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

			// the condition must be a key condition
			String key = PathCondition.getKeyCondition(cond);
			if (StringUtils.isEmpty(key)) {
				String msg = String.format("A key path condition should be defined for slot %s (not %s)", getName(stclContext), key);
				return StencilUtils.< StclContext, PStcl> iterator(Result.error(msg));
			}

			// gets month index
			String[] str = StringUtils.split(key, PathUtils.MULTI);
			int day = 0;
			day = Integer.parseInt(str[0]);
			if (day < 0 || day > 6) {
				String msg = String.format("The day should be defined between 0 and 6 (not %s)", day);
				return StencilUtils.< StclContext, PStcl> iterator(Result.error(msg));
			}

			// creates date format
			String format = (str.length == 1) ? "%ta" : str[1];
			Calendar cal = Calendar.getInstance(TimeZone.getDefault(), stclContext.getLocale());
			cal.set(0, 0, day + 3); // the date 0 was a Thursday
			String name = String.format(stclContext.getLocale(), format, cal.getTime());

			// creates the property
			StclFactory factory = (StclFactory) stclContext.getStencilFactory();
			PStcl prop = factory.createPProperty(stclContext, (PSlot<StclContext, PStcl>) null, Key.NO_KEY, name);
			return StencilUtils.< StclContext, PStcl> iterator(stclContext, prop, self);
		}
	}

	private class CalendarSlot extends MultiCalculatedSlot<StclContext, PStcl> {
		private int _field;

		public CalendarSlot(StclContext stclContext, String name, int field) {
			super(stclContext, DateStcl.this, name, PSlot.ANY);
			this._field = field;
		}

		@Override
		protected StencilIterator<StclContext, PStcl> getStencilsList(StclContext stclContext, StencilCondition<StclContext, PStcl> cond, PSlot<StclContext, PStcl> self) {

			// gets the date
			Date date;
			String time = PathCondition.getKeyCondition(cond);
			if (StringUtils.isEmpty(time)) {
				date = new Date();
			} else {
				try {
					date = new Date(Long.parseLong(time));
				} catch (Exception e) {
					String msg = String.format("A time long should be defined for slot %s (not %s)", getName(stclContext), time);
					return StencilUtils.< StclContext, PStcl> iterator(Result.error(msg));
				}
			}

			// gets the month index
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int field = cal.get(this._field);

			// creates the property
			StclFactory factory = (StclFactory) stclContext.getStencilFactory();
			PStcl prop = factory.createPProperty(stclContext, (PSlot<StclContext, PStcl>) null, Key.NO_KEY, field);
			return StencilUtils.< StclContext, PStcl> iterator(stclContext, prop, self);
		}
	}

}