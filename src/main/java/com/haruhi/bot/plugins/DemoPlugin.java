package com.haruhi.bot.plugins;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.extend.IPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 这是插件演示类
 * 必须实现plugin接口
 * 必须修饰@Component
 */
@Component
@Slf4j
public class DemoPlugin implements IPlugin {

    DemoPlugin(){
        // 可以在构造方法中打印日志，以观察插件是否被加载
        log.info("演示插件加载...");
    }

    /**
     * 收到私聊消息时触发
     * @param var
     * @param command 对方发送的消息
     */
    @Override
    public void onPrivateMessage(JSONObject var,String command) {
        /**
         * 发送人的qq
         * String user_id = var.getString("user_id");
         * // 发送人 发送的消息 实际上就是命令 你可以对其做一些匹配判断....
         * String message = command;
         * Client.sendMessage(user_id, MessageTypeEnum.privat,message, GocqActionEnum.SEND_MSG,true);
         */
    }

    /**
     * 收到群消息时触发
     * @param var
     * @param command
     */
    @Override
    public void onGroupMessage(JSONObject var,String command) {
        // 同上
    }
}
