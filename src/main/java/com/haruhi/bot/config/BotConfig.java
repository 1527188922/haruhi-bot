package com.haruhi.bot.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class BotConfig {

    public static AtomicBoolean SLEEP;
    public static String SELF_ID;
    public static String SUPER_USER = "";
    public static String NAME = "";
    public static String SEARCH_IMAGE_KEY = "";
    public static String HTTP_URL = "";
    public static String WS_URL = "";
    public static String ACCESS_TOKEN = "";
    static {
        SLEEP = new AtomicBoolean(false);
    }
    @Autowired
    public void setSuperUser(@Value("${bot.super-user}") String superUser) {
        SUPER_USER = superUser;
        if(Strings.isBlank(SUPER_USER)){
            log.warn("未配置超级用户qq，某些功能将无法使用（比如禁用/开启功能）");
        }
    }
    @Autowired
    public void setName(@Value("${bot.name}") String name) {
        NAME = Strings.isBlank(name) ? "春日酱" : name;
    }
    @Autowired
    public void setSearchImageKey(@Value("${bot.search-image-key}") String searchImageKey){
        SEARCH_IMAGE_KEY = searchImageKey;
        if(Strings.isBlank(SEARCH_IMAGE_KEY)){
            log.warn("未配置识图key,无法使用识图功能");
        }
    }
    @Autowired
    public void setHttpUrl(@Value("${gocq.http}") String httpUrl){
        HTTP_URL = httpUrl;
        if(Strings.isBlank(HTTP_URL)){
            throw new IllegalArgumentException("未配置gocq.http！");
        }
    }
    @Autowired
    public void setGocqWs(@Value("${gocq.ws}") String wsUrl) {
        WS_URL = wsUrl;
        if(Strings.isBlank(WS_URL)){
            throw new IllegalArgumentException("未配置gocq.ws！");
        }
    }
    @Autowired
    public void setAccessToken(@Value("${gocq.access-token}") String accessToken) {
        ACCESS_TOKEN = accessToken;
    }

    @PostConstruct
    private void postConstruct(){
        if (!Strings.isBlank(ACCESS_TOKEN)) {
            WS_URL += "?access_token=" + ACCESS_TOKEN;
        }
    }

    public static String toJson(){
        return "{\"SLEEP\":" + SLEEP + ",\"SELF_ID\":\"" + SELF_ID + "\",\"SUPER_USER\":\"" + SUPER_USER + "\",\"NAME\":\"" + NAME + "\",\"SEARCH_IMAGE_KEY\":\"" + SEARCH_IMAGE_KEY +
                "\",\"HTTP_URL\":\"" + HTTP_URL + "\",\"WS_URL\":\"" + WS_URL + "\"}";
    }

}
