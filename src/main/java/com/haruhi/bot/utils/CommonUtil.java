package com.haruhi.bot.utils;

import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.simplerobot.modules.utils.KQCodeUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CommonUtil {
    private CommonUtil(){}
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
    public synchronized static int randomInt(int start,int end){
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

    public static boolean isAt(String userId,final String context) {
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
    public static String startsWithCommandReplace(final String command, RegexEnum regexEnum){
        String[] split = regexEnum.getValue().split("\\|");
        for (String s : split) {
            if (command.startsWith(s)) {
                return command.replace(s,"");
            }
        }
        return null;
    }

    /**
     * 根据cq码类型 参数类型 获取参数的值
     * @param message
     * @param typeEnum
     * @param paramKey
     * @return
     */
    public static List<String> getCqParams(String message,CqCodeTypeEnum typeEnum,String paramKey){
        List<String> params = null;
        KQCodeUtils instance = KQCodeUtils.getInstance();
        String[] cqs = instance.getCqs(message, typeEnum.getType());
        if (cqs != null && cqs.length > 0) {
            params = new ArrayList<>();
            for (String cq : cqs) {
                String paramVal = instance.getParam(cq, paramKey);
                if(Strings.isNotBlank(paramVal)){
                    params.add(paramVal);
                }
            }
        }
        return params;
    }

    /**
     * 将源List按照指定元素数量拆分为多个List
     *
     * @param source 源List
     * @param splitItemNum 每个List中元素数量
     */
    public static <T> List<List<T>> averageAssignList(List<T> source, int splitItemNum) {
        List<List<T>> result = new ArrayList<List<T>>();
        if (source != null && source.size() > 0 && splitItemNum > 0) {
            if (source.size() <= splitItemNum) {
                // 源List元素数量小于等于目标分组数量
                result.add(source);
            } else {
                // 计算拆分后list数量
                int splitNum = (source.size() % splitItemNum == 0) ? (source.size() / splitItemNum) : (source.size() / splitItemNum + 1);

                List<T> value = null;
                for (int i = 0; i < splitNum; i++) {
                    if (i < splitNum - 1) {
                        value = source.subList(i * splitItemNum, (i + 1) * splitItemNum);
                    } else {
                        // 最后一组
                        value = source.subList(i * splitItemNum, source.size());
                    }
                    result.add(value);
                }
            }
        }
        return result;
    }
    public static String uuid(){
        return UUID.randomUUID().toString().replace("-","");
    }
    public static int averageAssignListSize(int size,int num){
        if(size % num == 0){
            return size / num;
        }else{
            return size / num + 1;
        }
    }
}
