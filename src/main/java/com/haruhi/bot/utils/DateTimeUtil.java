package com.haruhi.bot.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class DateTimeUtil {

    public enum FormatEnum{
        yyyyMMddHHmmssSSS("yyyy-MM-dd HH:mm:ss SSS"),
        yyyyMMddHHmmss("yyyy-MM-dd HH:mm:ss"),
        yyyyMMddHH("yyyy-MM-dd HH"),
        yyyyMMdd("yyyy-MM-dd"),
        yyyyMM("yyyy-MM"),
        yyyy("yyyy");

        private String format;
        FormatEnum(String format){
            this.format = format;
        }
    }

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

    /**
     * 基于某个时间添加分
     * @param date
     * @param minute
     * @return
     */
    public static Date addMinute(Date date,int minute){
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.add(Calendar.MINUTE, minute);
        return instance.getTime();
    }

    /**
     * 基于某个时间添加时
     * @param date
     * @param hour
     * @return
     */
    public static Date addHour(Date date,int hour){
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.add(Calendar.HOUR, hour);
        return instance.getTime();
    }

    /**
     * 基于某个时间添加天
     * @param date
     * @param day
     * @return
     */
    public static Date addDay(Date date,int day){
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.add(Calendar.DATE, day);
        return instance.getTime();
    }

    /**
     * 对某个时间进行格式化
     * 再获取Date对象
     * @return
     */
    public static Date formatToDate(Date date,DateTimeUtil.FormatEnum formatEnum){
        SimpleDateFormat df = new SimpleDateFormat(formatEnum.format);
        String time = df.format(date);
        try {
            return df.parse(time);
        } catch (ParseException e) {
            log.error("转Date对象异常",e);
            return null;
        }
    }
    public static String dateTimeFormat(Date date,DateTimeUtil.FormatEnum formatEnum){
        SimpleDateFormat df = new SimpleDateFormat(formatEnum.format);
        return df.format(date);
    }
    public static String dateTimeFormat(long timestamp,DateTimeUtil.FormatEnum formatEnum){
        SimpleDateFormat df = new SimpleDateFormat(formatEnum.format);
        return df.format(new Date(timestamp));
    }
}
