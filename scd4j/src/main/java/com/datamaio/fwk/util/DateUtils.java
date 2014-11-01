/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2014 scd4j scd4j.tools@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.datamaio.fwk.util;

import static com.datamaio.fwk.util.StringUtils.isEmpty;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

	public static final DateFormat LONG_DATE = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.S");
	public static final DateFormat SHORT_DATE = new SimpleDateFormat("dd/MM/yyyy");
	
	static{
		LONG_DATE.setLenient(false);
		SHORT_DATE.setLenient(false);
	}
	
	public static String format(final Calendar date) {
		return format(date, SHORT_DATE);
	}
	
	public static String format(final Calendar date, DateFormat df) {
		return date == null ? null : df.format(date.getTime());
	}

	public static String format(final Date date) {
		return format(date, SHORT_DATE);
	}
	
	public static String format(final Date date, DateFormat df) {
		return date == null ? null : df.format(date);
	}
    
	public static Calendar truncate(Calendar calendar) {
		if (calendar == null) {
			return null;
		}
		Calendar truncatedCalendar = Calendar.getInstance();
		truncatedCalendar.clear();
		truncatedCalendar.set(calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
		return truncatedCalendar;
	}
    
    public static Calendar toCalendar(Date date){
    	if(date != null){
    		Calendar calendar = Calendar.getInstance();
    		calendar.setTime(date);
    		return calendar;
    	}
    	return null;
    }
    
    public static Date toDate(Calendar calendar){
    	if(calendar != null){
    		return calendar.getTime();
    	}
    	return null;
    }

    public static Calendar parse(final String date) {
    	return parse(date, SHORT_DATE);
    }
    
	public static Calendar parse(final String date, DateFormat df) {
		if (date == null) {
			return null;
		}
		Calendar retorno = Calendar.getInstance();
		retorno.setLenient(false);
		retorno.setTime(parseToDate(date, df));
		return retorno;
	}
	

    public static Date parseToDate(String date) {
    	return parseToDate(date, SHORT_DATE);
    }
    
    public static Date parseToDate(String date, DateFormat df) {
    	if (isEmpty(date)) {
			return null;
		}

		try {			
			return df.parse(date);
		} catch (ParseException e) {
			throw new RuntimeException("Data no formato incorreto: " + date, e);
		}
	}
    
    public static boolean isValid(String str) {
    	return isValid(str, false);
    }
    
    public static boolean isValid(String str, boolean allowNull) {
    	return isValid(str, allowNull, SHORT_DATE);
    }
    
    public static boolean isValid(String str, boolean allowNull, DateFormat df) {
    	if (isEmpty(str)) {
    		return allowNull;
    	}
    	
		try {
			df.parse(str);
			return true;
		} catch (ParseException ex) {
			return false;
		}
	}
        
    public static Calendar today() {
		return Calendar.getInstance();
	}
    
    public static Calendar lastDayOfMonth(Calendar date) {
    	return new GregorianCalendar(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.getActualMaximum(Calendar.DAY_OF_MONTH));
    }
    
    public static Integer difference(Calendar date) {
		return difference(truncate(today()), date);
	} 
	
    public static Integer difference(Calendar date, Calendar highDate) {
		Long dif = highDate.getTimeInMillis() - date.getTimeInMillis();
		Long days = dif / 86400000;
		return Integer.parseInt(days.toString());
	} 
    
}
