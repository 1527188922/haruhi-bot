package com.haruhi.bot.factory;


import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.*;

public class ThreadPoolFactory {

    private static Executor commandHandlerThreadPool = null;
//    private static Executor pluginThreadPool = null;
//    private static ScheduledThreadPoolExecutor scheduledThreadPool = null;
    private static Executor downloadThreadPool = null;

    public static Executor getCommandHandlerThreadPool(){
        if(commandHandlerThreadPool == null){
            commandHandlerThreadPool =  new ThreadPoolExecutor(4,4,1, TimeUnit.HOURS,new ArrayBlockingQueue(50),new CustomizableThreadFactory("pool-command-handler-"),new ThreadPoolExecutor.CallerRunsPolicy());
        }
        return commandHandlerThreadPool;
    }


//    public static Executor getPluginThreadPool(){
//        if(pluginThreadPool == null){
//            pluginThreadPool =  new ThreadPoolExecutor(4,4,1, TimeUnit.HOURS,new ArrayBlockingQueue(50),new CustomizableThreadFactory("-plugin-"),new ThreadPoolExecutor.CallerRunsPolicy());
//        }
//        return pluginThreadPool;
//    }
//    public static ScheduledThreadPoolExecutor getScheduledThreadPool(){
//        if(scheduledThreadPool == null){
//            scheduledThreadPool =  new ScheduledThreadPoolExecutor(2, new CustomizableThreadFactory("scheduled"));
//        }
//        return scheduledThreadPool;
//    }

    public static Executor getDownloadThreadPool(){
        if(commandHandlerThreadPool == null){
            commandHandlerThreadPool =  new ThreadPoolExecutor(4,4,60, TimeUnit.SECONDS,new ArrayBlockingQueue(3),new CustomizableThreadFactory("pool-download-"),new ThreadPoolExecutor.CallerRunsPolicy());
        }
        return commandHandlerThreadPool;
    }
}
