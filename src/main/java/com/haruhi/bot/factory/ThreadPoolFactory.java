package com.haruhi.bot.factory;


import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.*;

public class ThreadPoolFactory {
    private ThreadPoolFactory(){}
    private final static Executor commandHandlerThreadPool = new ThreadPoolExecutor(4,4,1, TimeUnit.HOURS,new ArrayBlockingQueue(10),new CustomizableThreadFactory("pool-command-handler-"),new ThreadPoolExecutor.CallerRunsPolicy());
//    private static ScheduledThreadPoolExecutor scheduledThreadPool = null;
    private static Executor downloadThreadPool = null;
    private final static Executor chatHistoryThreadPool = new ThreadPoolExecutor(1,1,6, TimeUnit.HOURS,new ArrayBlockingQueue(15),new CustomizableThreadFactory("pool-chat-history-"),new ThreadPoolExecutor.CallerRunsPolicy());

    public static Executor getCommandHandlerThreadPool(){
        return commandHandlerThreadPool;
    }


//    public static ScheduledThreadPoolExecutor getScheduledThreadPool(){
//        if(scheduledThreadPool == null){
//            scheduledThreadPool =  new ScheduledThreadPoolExecutor(2, new CustomizableThreadFactory("scheduled"));
//        }
//        return scheduledThreadPool;
//    }

    public synchronized static Executor getDownloadThreadPool(){
        if(downloadThreadPool == null){
            downloadThreadPool = new ThreadPoolExecutor(4,4,60, TimeUnit.SECONDS,new ArrayBlockingQueue(5),new CustomizableThreadFactory("pool-download-"),new ThreadPoolExecutor.CallerRunsPolicy());
        }
        return downloadThreadPool;
    }
    public static Executor getChatHistoryThreadPool(){
        return chatHistoryThreadPool;
    }
}
