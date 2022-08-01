package com.haruhi.bot.utils;

import java.util.Random;

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
        if(cs == null || "".equals(cs.toString().trim())){
            return true;
        }
        return false;
    }
    private static Random random;
    public static int randomInt(int start,int end){
        if(random == null){
            random = new Random();
        }
        return random.nextInt(end - start + 1) + start;
    }
}
