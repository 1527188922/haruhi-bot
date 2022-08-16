package com.haruhi.bot.utils;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import org.apache.logging.log4j.util.Strings;

import java.util.HashMap;
import java.util.Map;

public class GocqRequestUtil {

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
}
