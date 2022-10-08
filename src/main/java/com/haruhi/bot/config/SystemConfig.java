package com.haruhi.bot.config;

import com.haruhi.bot.constant.OSEnum;
import com.haruhi.bot.factory.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SystemConfig {

    public static AtomicBoolean PRO;
    public static String PID;
    public static OSEnum osName;
    public static int availableProcessors;

    static {
        PRO = new AtomicBoolean(true);
        PID = getPID();
        osName = getOsName();
        availableProcessors = getAvailableProcessors();
        ThreadPoolFactory.resetThreadPoolSize();
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

    private static int getAvailableProcessors(){
        int i = Runtime.getRuntime().availableProcessors();
        log.info("cpu线程数:{}",i);
        return i;
    }

}
