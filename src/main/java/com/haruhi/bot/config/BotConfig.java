package com.haruhi.bot.config;

import com.haruhi.bot.constant.OSEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class BotConfig {
    public static AtomicBoolean SLEEP;
    public static AtomicBoolean PRO;
    public static OSEnum osName;
    public static String PID;
    public static String SELF_ID;
    public static String SUPER_USER = "";
    public static String NAME = "";
    public static String SEARCH_IMAGE_KEY = "";
    public static String HTTP_URL = "";
    static {
        SLEEP = new AtomicBoolean(false);
        PRO = new AtomicBoolean(true);
        PID = getPID();
        osName = getOsName();
    }
    @Autowired
    public void setSuperUser(@Value("${bot.super-user}") String superUser) {
        SUPER_USER = superUser;
    }
    @Autowired
    public void setName(@Value("${bot.name}") String name) {
        NAME = Strings.isBlank(name) ? "春日酱1" : name;
    }
    @Autowired
    public void setSearchImageKey(@Value("${bot.search-image-key}") String searchImageKey){
        SEARCH_IMAGE_KEY = searchImageKey;
    }
    @Autowired
    public void setHttpUrl(@Value("${gocq.http}") String httpUrl){
        HTTP_URL = httpUrl;
    }

    private static String getPID(){
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        String pid = bean.getName().split("@")[0];
        log.info("haruhi-bot pid : {}",pid);
        return pid;
    }

    private static OSEnum getOsName(){
        OSEnum os;
        String property = System.getProperty("os.name");
        if (property != null && property.toLowerCase().contains("linux")) {
            os = OSEnum.linux;
        }else {
            os = OSEnum.windows;
        }
        log.info("os name : {}",os.name());
        return os;
    }
}
