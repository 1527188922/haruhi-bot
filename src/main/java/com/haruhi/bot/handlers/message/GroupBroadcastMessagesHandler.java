package com.haruhi.bot.handlers.message;

import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.response.GroupInfo;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IPrivateMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.GocqRequestUtil;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.List;

@Slf4j
@Component
public class GroupBroadcastMessagesHandler implements IPrivateMessageEvent {
    @Override
    public int weight() {
        return 86;
    }

    @Override
    public String funName() {
        return "群广播";
    }

    @Override
    public boolean onPrivate(final Message message,final String command) {
        if(!message.getUser_id().equals(BotConfig.SUPER_USER)){
            return false;
        }
        String msg = CommonUtil.commandReplaceFirst(command,RegexEnum.GROUP_BROADCAST_MESSAGES);
        if(Strings.isBlank(msg)){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new Task(message,msg));
        return true;
    }

    private class Task implements Runnable{
        private Message message;
        private String msg;

        Task(Message message,String msg){
            this.message = message;
            this.msg = msg;
        }
        @Override
        public void run() {
            List<GroupInfo> groupList = GocqRequestUtil.getGroupList();
            if(CollectionUtils.isEmpty(groupList)){
                Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageEventEnum.privat, MessageFormat.format("{0}还没有加任何群。。",BotConfig.NAME), GocqActionEnum.SEND_MSG,true);
                return;
            }
            for (GroupInfo groupInfo : groupList) {
                Client.sendMessage(message.getUser_id(),groupInfo.getGroup_id(), MessageEventEnum.group, MessageFormat.format("※来自bot管理员的群广播消息：\n{1}",message.getUser_id(),msg), GocqActionEnum.SEND_MSG,false);
            }
            Client.sendMessage(message.getUser_id(),null, MessageEventEnum.privat, "发送完成", GocqActionEnum.SEND_MSG,false);
        }
    }
}
