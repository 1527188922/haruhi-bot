package com.haruhi.bot.utils.system;

import com.haruhi.bot.factory.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 系统信息以及程序信息
 * 存放一些固定不变的值
 */
@Slf4j
public class SystemInfo {

    public static final String PROPERTY_SPECIFICATION_NAME = "java.specification.name";
    public static final String PROPERTY_VERSION = "java.version";
    public static final String PROPERTY_SPECIFICATION_VERSION = "java.specification.version";
    public static final String PROPERTY_VENDOR = "java.vendor";
    public static final String PROPERTY_SPECIFICATION_VENDOR = "java.specification.vendor";
    public static final String PROPERTY_VENDOR_URL = "java.vendor.url";
    public static final String PROPERTY_HOME = "java.home";
    public static final String PROPERTY_LIBRARY_PATH = "java.library.path";
    public static final String PROPERTY_TMPDIR = "java.io.tmpdir";
    public static final String PROPERTY_COMPILER = "java.compiler";
    public static final String PROPERTY_EXT_DIRS = "java.ext.dirs";
    public static final String PROPERTY_VM_NAME = "java.vm.name";
    public static final String PROPERTY_VM_SPECIFICATION_NAME = "java.vm.specification.name";
    public static final String PROPERTY_VM_VERSION = "java.vm.version";
    public static final String PROPERTY_VM_SPECIFICATION_VERSION = "java.vm.specification.version";
    public static final String PROPERTY_VM_VENDOR = "java.vm.vendor";
    public static final String PROPERTY_VM_SPECIFICATION_VENDOR = "java.vm.specification.vendor";
    public static final String PROPERTY_CLASS_VERSION = "java.class.version";
    public static final String PROPERTY_CLASS_PATH = "java.class.path";
    public static final String PROPERTY_OS_NAME = "os.name";
    public static final String PROPERTY_OS_ARCH = "os.arch";
    public static final String PROPERTY_OS_VERSION = "os.version";
    public static final String PROPERTY_FILE_SEPARATOR = "file.separator";
    public static final String PROPERTY_PATH_SEPARATOR = "path.separator";
    public static final String PROPERTY_LINE_SEPARATOR = "line.separator";
    public static final String PROPERTY_USER_NAME = "user.name";
    public static final String PROPERTY_USER_HOME = "user.home";
    public static final String PROPERTY_USER_DIR = "user.dir";

    public static AtomicBoolean PRO;
    public static String PID;
    public static String OS_NAME;
    public static int AVAILABLE_PROCESSORS;
    public static File DISK;
    private static long TOTAL_SPACE;
    private static long TOTAL_SPACE_GB;

    static {
        init();
    }
    private static void init(){
        PRO = new AtomicBoolean(true);
        getPID();
        getOsName();
        getAvailableProcessors();
        ThreadPoolFactory.resetThreadPoolSize();
        getDisk();
        getTotalSpace();
    }

    private static void getPID(){
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        PID = bean.getName().split("@")[0];
        log.info("haruhi-bot pid : {}",PID);
    }

    private static void getOsName(){
        OS_NAME = SystemUtil.OS_NAME;
        log.info("os name : {}",OS_NAME);
    }

    private static void getAvailableProcessors(){
        AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
        log.info("cpu线程数 : {}",AVAILABLE_PROCESSORS);
    }

    private static void getDisk(){
        for (File file : File.listRoots()) {
            if (SystemUtil.USER_DIR.startsWith(file.toString())) {
                DISK = file;
                break;
            }
        }
        log.info("disk : {}",DISK);
    }
    private static void getTotalSpace(){
        if(DISK != null){
            TOTAL_SPACE = DISK.getTotalSpace();
            TOTAL_SPACE_GB = TOTAL_SPACE / 1024L / 1024L / 1024L;
            log.info("total space : {}GB",TOTAL_SPACE_GB);
        }
    }

}
