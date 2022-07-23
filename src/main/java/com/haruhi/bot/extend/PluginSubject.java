package com.haruhi.bot.extend;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.thread.PluginTask;

import java.util.HashSet;
import java.util.Set;

public class PluginSubject {
    private static Set<IPlugin> container = new HashSet<>();

    public static void attach(IPlugin plugin){
        container.add(plugin);
    }

    public static void detach(IPlugin plugin){
        container.remove(plugin);
    }

    public static void update(JSONObject json,String command){
        String messageType = json.getString("message_type");
        for (IPlugin plugin : container) {
            ThreadPoolFactory.getPluginThreadPool().execute(new PluginTask(plugin,messageType,command,json));
        }

    }
}
