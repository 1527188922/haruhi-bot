package com.haruhi.bot.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;

import com.haruhi.bot.dto.gocq.response.GroupInfo;
import com.haruhi.bot.dto.gocq.response.GroupMember;
import com.haruhi.bot.dto.gocq.response.HttpResponse;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.response.SelfInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class GocqRequestUtil {
    private GocqRequestUtil(){}
    /**
     * 获取消息详情对象
     * @param messageId 消息id
     * @return
     */
    public static Message getMsg(String messageId){
        Map<String, Object> map = new HashMap<>(2);
        map.put("message_id",messageId);
        map.put("access_token",BotConfig.ACCESS_TOKEN);
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
    public static List<GroupMember> getGroupMemberList(String groupId, String... exclude){
        Map<String, Object> params = new HashMap<>(2);
        params.put("group_id",groupId);
        params.put("access_token",BotConfig.ACCESS_TOKEN);
        String responseStr = RestUtil.sendGetRequest(RestUtil.getRestTemplate(), BotConfig.HTTP_URL + "/" + GocqActionEnum.GET_GROUP_MEMBER_LIST.getAction(), params, String.class);
        if (responseStr == null) {
            return null;
        }
        JSONObject responseJsonObj = JSONObject.parseObject(responseStr);
        List<GroupMember> data = JSONArray.parseArray(responseJsonObj.getString("data"), GroupMember.class);
        if(exclude != null && exclude.length > 0){
            List<GroupMember> excludeList = new ArrayList<>(exclude.length);
            for (GroupMember datum : data) {
                for (String s : exclude) {
                    if(datum != null && s.equals(datum.getUser_id())){
                        excludeList.add(datum);
                    }
                }
            }
            if (excludeList.size() > 0) {
                data.removeAll(excludeList);
            }
        }
        return data;
    }

    /**
     * 分词
     * @param content 语句
     * @return
     */
    public static List<String> getWordSlices(String content){
        Map<String, Object> req = new HashMap<>(2);
        req.put("content",content);
        req.put("access_token",BotConfig.ACCESS_TOKEN);
        HttpResponse httpResponse = RestUtil.sendGetRequest(RestUtil.getRestTemplate(10 * 1000), BotConfig.HTTP_URL + "/" + GocqActionEnum.GET_WORD_SLICES.getAction(), req, HttpResponse.class);
        if(httpResponse != null && httpResponse.getRetcode() == 0 ){
            HttpResponse.RespData data = httpResponse.getData();
            return data != null ? data.getSlices() : null;
        }
        return null;
    }

    /**
     * 获取群列表
     * 无需要参数 只能获取登录于go-cqhttp的账号的群组集合
     * @return
     */
    public static List<GroupInfo> getGroupList(){
        Map<String, Object> req = new HashMap<>(1);
        req.put("access_token",BotConfig.ACCESS_TOKEN);
        String responseStr = RestUtil.sendGetRequest(RestUtil.getRestTemplate(),BotConfig.HTTP_URL + "/" + GocqActionEnum.GET_GROUP_LIST.getAction(), req, String.class);
        if (Strings.isNotBlank(responseStr)) {
            String listStr = JSONObject.parseObject(responseStr).getString("data");
            if (Strings.isNotBlank(listStr)) {
                return JSONArray.parseArray(listStr, GroupInfo.class);
            }
        }
        return null;
    }

    public static SelfInfo getLoginInfo(){
        Map<String, Object> req = new HashMap<>(1);
        req.put("access_token",BotConfig.ACCESS_TOKEN);
        String responseStr = RestUtil.sendGetRequest(RestUtil.getRestTemplate(),BotConfig.HTTP_URL + "/" + GocqActionEnum.GET_LOGIN_INGO.getAction(), req, String.class);
        if (Strings.isNotBlank(responseStr)) {
            JSONObject jsonObject = JSONObject.parseObject(responseStr);
            SelfInfo data = JSONObject.parseObject(jsonObject.getString("data"), SelfInfo.class);
            return data;
        }
        return null;
    }
}
