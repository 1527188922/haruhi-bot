package com.haruhi.bot.handlers.message.chatHistory;

import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.entity.GroupChatHistory;
import com.haruhi.bot.event.message.IGroupMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.groupChatHistory.GroupChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GroupChatHistoryAddHandler implements IGroupMessageEvent {
    @Override
    public int weight() {
        return 997;
    }
    private final static GroupChatHistory param = new GroupChatHistory();
    @Autowired
    private GroupChatHistoryService groupChatHistoryService;

    /**
     * 群聊历史聊天入库
     * 不参与命令处理,最终返回false
     * @param message
     * @param command
     * @return
     */
    @Override
    public boolean onGroup(final Message message,final String command) {
        ThreadPoolFactory.getChatHistoryThreadPool().execute(new GroupChatHistoryAddHandler.Task(groupChatHistoryService,message,param));
        return false;
    }

    public static class Task implements Runnable{
        private GroupChatHistoryService service;
        private GroupChatHistory param;
        public Task(GroupChatHistoryService service,final Message message,final GroupChatHistory param){
            this.service = service;
            param.setId(null);
            param.setCard(message.getSender().getCard());
            param.setNickname(message.getSender().getNickname());
            param.setGroupId(message.getGroup_id());
            param.setUserId(message.getUser_id());
            param.setContent(message.getMessage());
            param.setCreateTime(message.getTime() * 1000);
            param.setMessageId(message.getMessage_id());
            this.param = param;
        }

        @Override
        public void run() {
            try {
                service.save(param);
            }catch (Exception e){
                log.error("群聊天历史入库异常",e);
            }
        }
    }
}
