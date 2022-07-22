package com.haruhi.bot.utils;

import java.util.Calendar;
import java.util.Date;

public class DateTimeUtil {

    public static boolean isSameDay(Date var1, Date var2){
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(var1);
        cal2.setTime(var2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
