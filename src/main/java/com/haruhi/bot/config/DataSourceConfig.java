package com.haruhi.bot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 不需要在这里进行配置数据库（配置了不会生效）
 * 这个类会自己读取配置文件
 *
 * 之所以这样写，是为了让静态变量能够读到配置文件
 */
@Component
public class DataSourceConfig {

    // 主数据库 数据源名称
    public final static String DATA_SOURCE_MASTER_NAME = "master";
    // 主数据库名称 必须为mysql 这是mysql的系统库
    public static String DATA_BASE_MASTER_NAME = "";
    // 驱动类全命名 reference
    public static String DATA_BASE_MASTER_DRIVERCLASSNAME = "";


    public static String jdbcUrlTemplate = "jdbc:mysql://{0}:{1}/{2}?useUnicode=true&characterEncoding=utf-8&useSSL=false";
    // bot数据源名称
    public final static String DATA_SOURCE_BOT_NAME = "bot";
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
    public final static String BOT_T_DISABLE_HANDLER = "t_disable_handler";
    // pixiv 图库表
    public final static String BOT_T_PIXIV = "t_pixiv";
    public final static String BOT_T_WORD_STRIP = "t_word_strip";

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
    }

    @Autowired
    public void setUsername(@Value("${mysql.username}") String username) {
        DATA_BASE_BOT_USERNAME = username;
    }
    @Autowired
    public void setPassword(@Value("${mysql.password}") String password) {
        DATA_BASE_BOT_PASSWORD = password;
    }
    @Autowired
    public void setHost(@Value("${mysql.host}") String host) {
        DATA_BASE_BOT_HOST = host;
    }
    @Autowired
    public void setPort(@Value("${mysql.port}") String port) {
        DATA_BASE_BOT_PORT = port;
    }

}
