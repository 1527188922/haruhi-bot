package com.haruhi.bot.utils;

import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.dto.gocq.response.ForwardMsg;
import com.simplerobot.modules.utils.KQCodeUtils;

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

    public static boolean isAt(String userId,String context) {
        KQCodeUtils instance = KQCodeUtils.getInstance();
        String[] cqs = instance.getCqs(context, CqCodeTypeEnum.at.getType());
        if (cqs == null || cqs.length == 0){
            return false;
        }
        for (String cq : cqs) {
            String qq = instance.getParam(cq, "qq");
            if(userId.equals(qq)){
                return true;
            }
        }
        return false;
    }
}
