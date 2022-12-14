package com.haruhi.bot.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;

/**
 * 不需要在这里进行配置数据库（配置了不会生效）
 * 这个类会自己读取配置文件
 *
 * 之所以这样写，是为了让静态变量能够读到配置文件
 */
@Component
@Slf4j
public class DataSourceConfig {

    // 系统数据源名称
    public final static String DATA_SOURCE_SYSTEM = "system";
    // 主数据库名称 必须为mysql 这是mysql的系统库
    public static String DATA_BASE_MASTER_NAME = "";
    // 驱动类全命名 reference
    public static String DATA_BASE_MASTER_DRIVERCLASSNAME = "";


    public static String jdbcUrlTemplate = "jdbc:mysql://{0}:{1}/{2}?useUnicode=true&characterEncoding=utf-8&useSSL=false";
    public static String JDBC_URL = "";
    // bot数据源名称
    public final static String DATA_SOURCE_MASTER = "master";
    // bot数据库名称
    public static String DATA_BASE_BOT = "";
    // 数据库用户名
    public static String DATA_BASE_BOT_USERNAME = "";
    // 数据库密码
    public static String DATA_BASE_BOT_PASSWORD = "";
    //  数据库 host
    public static String DATA_BASE_BOT_HOST = "";
    // 数据库 port
    public static String DATA_BASE_BOT_PORT = "";

    // 签到表
    public final static String BOT_T_CHECKIN = "t_checkin";
    // 禁用功能表
    public final static String BOT_T_DISABLE_FUNCTION = "t_disable_function";
    // pixiv 图库表
    public final static String BOT_T_PIXIV = "t_pixiv";
    // 词条
    public final static String BOT_T_WORD_STRIP = "t_word_strip";
    // 话术
    public final static String BOT_T_VERBAL_TRICKS = "t_verbal_tricks";
    // 群聊天历史
    public final static String BOT_T_GROUP_CHAT_HISTORY = "t_group_chat_history";
    // 戳一戳回复表
    public final static String BOT_T_POKE_REPLY = "t_poke_reply";
    // 订阅新闻表
    public final static String BOT_T_SUBSCRIBE_NEWS = "t_subscribe_news";

    @Autowired
    public void setSystemDbname(@Value("${spring.datasource.dynamic.datasource.master.dbName}") String dbName) {
        DATA_BASE_MASTER_NAME = dbName;
    }

    @Autowired
    public void setSystemDriverClassName(@Value("${spring.datasource.dynamic.datasource.master.driver-class-name}") String driverClassName) {
        DATA_BASE_MASTER_DRIVERCLASSNAME = driverClassName;
    }

    @Autowired
    public void setBotDbname(@Value("${mysql.dbName}") String dbName) {
        DATA_BASE_BOT = dbName;
        if (Strings.isBlank(DATA_BASE_BOT)) {
            throw new IllegalArgumentException("未配置数据库名称！");
        }
    }

    @Autowired
    public void setUsername(@Value("${mysql.username}") String username) {
        DATA_BASE_BOT_USERNAME = username;
        if (Strings.isBlank(DATA_BASE_BOT_USERNAME)) {
            throw new IllegalArgumentException("未配置数据库用户名！");
        }
    }
    @Autowired
    public void setPassword(@Value("${mysql.password}") String password) {
        DATA_BASE_BOT_PASSWORD = password;
        if (Strings.isBlank(DATA_BASE_BOT_PASSWORD)) {
            throw new IllegalArgumentException("未配置数据库密码！");
        }
    }
    @Autowired
    public void setHost(@Value("${mysql.host}") String host) {
        DATA_BASE_BOT_HOST = host;
        if (Strings.isBlank(DATA_BASE_BOT_HOST)) {
            throw new IllegalArgumentException("未配置数据库主机地址！");
        }
    }
    @Autowired
    public void setPort(@Value("${mysql.port}") String port) {
        DATA_BASE_BOT_PORT = port;
        if (Strings.isBlank(DATA_BASE_BOT_PORT)) {
            throw new IllegalArgumentException("未配置数据库端口！");
        }
    }

    @PostConstruct
    private void postConstruct(){
        JDBC_URL = MessageFormat.format(jdbcUrlTemplate,DATA_BASE_BOT_HOST,DATA_BASE_BOT_PORT,DATA_BASE_BOT);
        log.info("数据源配置加载并校验完成:{}",JDBC_URL);
    }
}
