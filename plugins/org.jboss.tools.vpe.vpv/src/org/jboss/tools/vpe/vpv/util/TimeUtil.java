package org.jboss.tools.vpe.vpv.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.jboss.tools.vpe.vpv.Activator;

public class TimeUtil {
	
    public static final String HTTP_RESPONSE_DATE_HEADER = "EEE, dd MMM yyyy HH:mm:ss zzz";  
	
	public static String toGMTString(Date date) {
		DateFormat httpDateFormat = new SimpleDateFormat(HTTP_RESPONSE_DATE_HEADER);
		httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return httpDateFormat.format(date);
	} 
	
	public static Date toLocalDate(String string) {
		Date date = null;
		DateFormat httpDateFormat = new SimpleDateFormat(HTTP_RESPONSE_DATE_HEADER);
		try {
			date = httpDateFormat.parse(string);
		} catch (ParseException e) {
			Activator.logError(e);
		}
		return date;
	}
}
