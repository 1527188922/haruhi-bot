package com.haruhi.bot.handlers.message.chatHistory;

import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.entity.GroupChatHistory;
import com.haruhi.bot.event.message.IGroupMessageEvent;
import com.haruhi.bot.service.groupChatHistory.GroupChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GroupChatHistoryAddHandler implements IGroupMessageEvent {
    @Override
    public int weight() {
        return 997;
    }

    @Override
    public String funName() {
        return "群聊记录入库";
    }

    @Autowired
    private GroupChatHistoryService groupChatHistoryService;

    private static ExecutorService threadPool;
    public GroupChatHistoryAddHandler(){
        if (threadPool == null) {
            threadPool = new ThreadPoolExecutor(1, 1,15L, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>(),new CustomizableThreadFactory("pool-insertRecord-"));
        }
    }
    /**
     * 群聊历史聊天入库
     * 不参与命令处理,最终返回false
     * @param message
     * @param command
     * @return
     */
    @Override
    public boolean onGroup(final Message message,final String command) {
        threadPool.execute(new Task(groupChatHistoryService,message));
        return false;
    }

    private class Task implements Runnable{
        private GroupChatHistoryService service;
        private Message message;
        public Task(GroupChatHistoryService service,final Message message){
            this.service = service;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                GroupChatHistory param = new GroupChatHistory();
                param.setId(null);
                param.setCard(message.getSender().getCard());
                param.setNickname(message.getSender().getNickname());
                param.setGroupId(message.getGroupId());
                param.setUserId(message.getUserId());
                param.setContent(message.getMessage());
                param.setCreateTime(message.getTime() * 1000);
                param.setMessageId(message.getMessageId());
                service.save(param);
            }catch (Exception e){
                log.error("群聊天历史入库异常",e);
            }
        }
    }
}
