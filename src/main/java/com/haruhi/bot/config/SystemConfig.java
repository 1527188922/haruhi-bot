package com.haruhi.bot.config;

import com.haruhi.bot.factory.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SystemConfig {

    public static AtomicBoolean PRO;
    public static String PID;
    public static String osName;
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

    private static String getOsName(){
        String osName = SystemUtils.OS_NAME;
        log.info("os name : {}",osName);
        return osName;
    }

    private static int getAvailableProcessors(){
        int i = Runtime.getRuntime().availableProcessors();
        log.info("cpu线程数 : {}",i);
        return i;
    }

}
