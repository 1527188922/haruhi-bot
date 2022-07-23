package com.haruhi.bot.handlers.command;

import com.alibaba.fastjson.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class Subject {

    private static Set<AbstractCommandHandler> container = new HashSet<>();

    public static void attach(AbstractCommandHandler handler){
        container.add(handler);
    }

    public static void detach(AbstractCommandHandler handler){
        container.remove(handler);
    }

    public static void update(JSONObject json,String command){
        for (AbstractCommandHandler handler : container) {
            handler.matches(json,command);
        }
    }

}
