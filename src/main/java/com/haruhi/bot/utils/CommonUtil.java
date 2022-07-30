package com.haruhi.bot.utils;

public class CommonUtil {

    private static String separator = "-";
    public static String getKey(String userId,String groupId){
        return userId + separator + groupId;
    }

    public static String getUserIdFromKey(String key){
        return key.split(separator)[0];
    }
    public static String getGroupIdFromKey(String key){
        return key.split(separator)[1];
    }

    public static boolean isBlank(final CharSequence cs){
        if(cs == null || cs.toString().trim() == ""){
            return true;
        }
        return false;
    }
}
