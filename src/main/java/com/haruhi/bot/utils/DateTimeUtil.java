package com.haruhi.bot.utils;

import java.util.Calendar;
import java.util.Date;

public class DateTimeUtil {

    /**
     * 两个Date对象是否为同一天
     * @param var1
     * @param var2
     * @return
     */
    public static boolean isSameDay(Date var1, Date var2){
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(var1);
        cal2.setTime(var2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static Date addHour(Date current,int hour){
        Calendar instance = Calendar.getInstance();
        instance.setTime(current);
        instance.add(Calendar.MINUTE, hour);
        return instance.getTime();
    }
    public static Date addDay(Date current,int day){
        Calendar instance = Calendar.getInstance();
        instance.setTime(current);
        instance.add(Calendar.DATE, day);
        return instance.getTime();
    }
}
