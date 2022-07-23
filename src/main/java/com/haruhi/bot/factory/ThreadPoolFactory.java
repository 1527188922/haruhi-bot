package com.haruhi.bot.factory;


import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolFactory {

    private static Executor commandHandlerThreadPool = null;
    private static Executor pluginThreadPool = null;

    public static Executor getCommandHandlerThreadPool(){
        if(commandHandlerThreadPool == null){
            commandHandlerThreadPool =  new ThreadPoolExecutor(3,4,1, TimeUnit.HOURS,new ArrayBlockingQueue(20),new CustomizableThreadFactory("-command-handler-"),new ThreadPoolExecutor.CallerRunsPolicy());
        }
        return commandHandlerThreadPool;
    }

    public static Executor getPluginThreadPool(){
        if(pluginThreadPool == null){
            pluginThreadPool =  new ThreadPoolExecutor(3,4,1, TimeUnit.HOURS,new ArrayBlockingQueue(20),new CustomizableThreadFactory("-plugin-"),new ThreadPoolExecutor.CallerRunsPolicy());
        }
        return pluginThreadPool;
    }
}
