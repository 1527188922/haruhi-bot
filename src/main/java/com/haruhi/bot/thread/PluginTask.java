package com.haruhi.bot.thread;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.extend.IPlugin;

public class PluginTask implements Runnable{

    private IPlugin plugin;
    private String messageType;
    private String command;
    private JSONObject json;

    public PluginTask(IPlugin plugin,String messageType,String command,JSONObject json){
        this.command = command;
        this.plugin = plugin;
        this.json = json;
        this.messageType = messageType;
    }
    @Override
    public void run() {
        if(MessageTypeEnum.group.getType().equals(messageType)){
            plugin.onGroupMessage(json,command);
        }else if(MessageTypeEnum.privat.getType().equals(messageType)){
            plugin.onPrivateMessage(json,command);
        }

    }
}
