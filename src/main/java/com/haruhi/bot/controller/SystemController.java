package com.haruhi.bot.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.config.path.AbstractPathConfig;
import com.haruhi.bot.dto.http.HttpResponse;
import com.haruhi.bot.utils.system.SystemInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sys")
public class SystemController {

    @Autowired
    private AbstractPathConfig abstractPathConfig;

    @RequestMapping("/status")
    public HttpResponse<Map> status(){
        Map<String, JSONObject> resp = new HashMap<>(3);
        resp.put(BotConfig.class.getSimpleName(), JSON.parseObject(BotConfig.toJson()));
        resp.put(SystemInfo.class.getSimpleName(),JSON.parseObject(SystemInfo.toJson()));
        resp.put(AbstractPathConfig.class.getSimpleName(),JSON.parseObject(abstractPathConfig.toString()));

        return HttpResponse.success(resp);
    }
}
