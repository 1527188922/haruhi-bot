package com.haruhi.bot.factory;


import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.*;

public class ThreadPoolFactory {

    private static Executor commandHandlerThreadPool = null;
//    private static ScheduledThreadPoolExecutor scheduledThreadPool = null;
    private static Executor downloadThreadPool = null;
    private static Executor chatHistoryThreadPool = null;

    public static Executor getCommandHandlerThreadPool(){
        if(commandHandlerThreadPool == null){
            commandHandlerThreadPool =  new ThreadPoolExecutor(4,4,1, TimeUnit.HOURS,new ArrayBlockingQueue(10),new CustomizableThreadFactory("pool-command-handler-"),new ThreadPoolExecutor.CallerRunsPolicy());
        }
        return commandHandlerThreadPool;
    }


//    public static ScheduledThreadPoolExecutor getScheduledThreadPool(){
//        if(scheduledThreadPool == null){
//            scheduledThreadPool =  new ScheduledThreadPoolExecutor(2, new CustomizableThreadFactory("scheduled"));
//        }
//        return scheduledThreadPool;
//    }

    public static Executor getDownloadThreadPool(){
        if(downloadThreadPool == null){
            downloadThreadPool =  new ThreadPoolExecutor(4,4,60, TimeUnit.SECONDS,new ArrayBlockingQueue(5),new CustomizableThreadFactory("pool-download-"),new ThreadPoolExecutor.CallerRunsPolicy());
        }
        return downloadThreadPool;
    }
    public static Executor getChatHistoryThreadPool(){
        if(chatHistoryThreadPool == null){
            chatHistoryThreadPool =  new ThreadPoolExecutor(1,1,30, TimeUnit.MINUTES,new ArrayBlockingQueue(15),new CustomizableThreadFactory("pool-chat-history-"),new ThreadPoolExecutor.CallerRunsPolicy());
        }
        return chatHistoryThreadPool;
    }
}
