package com.haruhi.bot.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.dto.gocq.request.GroupMember;
import com.haruhi.bot.dto.gocq.response.HttpResponse;
import com.haruhi.bot.dto.gocq.response.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

@Slf4j
public class GocqRequestUtil {

    /**
     * 获取消息详情对象
     * @param messageId 消息id
     * @return
     */
    public static Message getMsg(String messageId){
        Map<String, Object> map = new HashMap<>();
        map.put("message_id",messageId);
        String s = RestUtil.sendGetRequest(RestUtil.getRestTemplate(), BotConfig.HTTP_URL + "/" + GocqActionEnum.GET_MSG.getAction(), map, String.class);
        if (Strings.isNotBlank(s)) {
            JSONObject jsonObject = JSONObject.parseObject(s);
            return JSONObject.parseObject(jsonObject.getString("data"), Message.class);
        }
        return null;
    }

    /**
     * 获取群成员
     * @param groupId 群号
     * @param exclude 需要排除的成员qq号
     * @return
     */
    public static List<GroupMember> getGroupMemberList(String groupId,String... exclude){
        Map<String, Object> params = new HashMap<>();
        params.put("group_id",groupId);
        String responseStr = RestUtil.sendPostRequest(RestUtil.getRestTemplate(), BotConfig.HTTP_URL + "/" + GocqActionEnum.GET_GROUP_MEMBER_LIST.getAction(), params, null, String.class);
        if (responseStr == null) {
            return null;
        }
        JSONObject responseJsonObj = JSONObject.parseObject(responseStr);
        List<GroupMember> data = JSONArray.parseArray(responseJsonObj.getString("data"), GroupMember.class);
        if(exclude != null && exclude.length > 0){
            List<GroupMember> excludeList = new ArrayList<>();
            for (GroupMember datum : data) {
                for (String s : exclude) {
                    if(datum != null && s.equals(datum.getUser_id())){
                        excludeList.add(datum);
                    }
                }
            }
            data.removeAll(excludeList);
        }
        return data;
    }

    /**
     * 分词
     * @param content 语句
     * @return
     */
    public static List<String> getWordSlices(String content){
        Map<String, Object> req = new HashMap<>();
        req.put("content",content);
        HttpResponse httpResponse = RestUtil.sendPostRequest(RestUtil.getRestTemplate(10 * 1000), BotConfig.HTTP_URL + "/" + GocqActionEnum.GET_WORD_SLICES.getAction(), req, null, HttpResponse.class);
        if(httpResponse != null && httpResponse.getRetcode() == 0 ){
            HttpResponse.RespData data = httpResponse.getData();
            return data != null ? data.getSlices() : null;
        }
        return null;
    }
}
