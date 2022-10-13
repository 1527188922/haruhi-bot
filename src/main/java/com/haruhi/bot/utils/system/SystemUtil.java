package com.haruhi.bot.utils.system;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.lang3.SystemUtils;

import java.lang.management.ManagementFactory;

public class SystemUtil extends SystemUtils {

    public static OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public synchronized static double getFreeSpace(){
        SystemInfo.FREE_SPACE = (double) SystemInfo.DISK.getFreeSpace();
        SystemInfo.FREE_SPACE_GB = SystemInfo.FREE_SPACE / 1024 / 1024 / 1024;
        return SystemInfo.FREE_SPACE;
    }

    public static double getFreePhysicalMemorySize(){
        SystemInfo.FREE_PHYSICAL_MEMORY_SIZE = osmxb.getFreePhysicalMemorySize();
        SystemInfo.FREE_PHYSICAL_MEMORY_SIZE_GB = SystemInfo.FREE_PHYSICAL_MEMORY_SIZE / 1024 / 1024 / 1024;
        return SystemInfo.FREE_PHYSICAL_MEMORY_SIZE;
    }
}
