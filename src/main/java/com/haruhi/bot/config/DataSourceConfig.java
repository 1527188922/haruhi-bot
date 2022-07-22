package com.haruhi.bot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 不需要在这里进行配置数据库（配置了不会生效）
 * 这个类会自己读取配置文件
 */
@Configuration
public class DataSourceConfig {

    // 主数据库 数据源名称
    public final static String DATA_SOURCE_MASTER_NAME = "master";
    // 主数据库名称 必须为mysql 这是mysql的系统库
    public static String DATA_BASE_MASTER_NAME = "";
    // 驱动类全命名 reference
    public static String DATA_BASE_MASTER_DRIVERCLASSNAME = "";


    // bot数据源名称
    public final static String DATA_SOURCE_BOT_NAME = "bot";
    // bot数据库名称
    public static String DATA_BASE_BOT = "";
    // jdbc url
    public static String DATA_BASE_BOT_URL = "";
    // 数据库用户名
    public static String DATA_BASE_BOT_USERNAME = "";
    // 数据库密码
    public static String DATA_BASE_BOT_PASSWORD = "";

    // 签到表
    public final static String BOT_T_CHECKIN = "t_checkin";


    @Autowired
    public void setSystemDbname(@Value("${spring.datasource.dynamic.datasource.master.dbName}") String dbName) {
        DATA_BASE_MASTER_NAME = dbName;
    }

    @Autowired
    public void setSystemDriverClassName(@Value("${spring.datasource.dynamic.datasource.master.driver-class-name}") String driverClassName) {
        DATA_BASE_MASTER_DRIVERCLASSNAME = driverClassName;
    }

    @Autowired
    public void setBotDbname(@Value("${bot.mysql.dbName}") String dbName) {
        DATA_BASE_BOT = dbName;
    }
    @Autowired
    public void setUrl(@Value("${bot.mysql.url}") String url) {
        DATA_BASE_BOT_URL = url;
    }
    @Autowired
    public void setUsername(@Value("${bot.mysql.username}") String username) {
        DATA_BASE_BOT_USERNAME = username;
    }
    @Autowired
    public void setPassword(@Value("${bot.mysql.password}") String password) {
        DATA_BASE_BOT_PASSWORD = password;
    }
}
