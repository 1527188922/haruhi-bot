package com.haruhi.bot.utils;

import com.haruhi.bot.dto.gocq.response.ForwardMsg;

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
    public static ForwardMsg createForwardMsgItem(String context,String uin,String name){
        ForwardMsg item = new ForwardMsg();
        ForwardMsg.Data data = new ForwardMsg.Data();
        data.setUin(uin);
        data.setName(name);
        data.setContent(context);
        item.setData(data);
        return item;
    }

}
