package com.haruhi.bot.handlers.message;

import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.entity.VerbalTricks;
import com.haruhi.bot.event.message.IOnMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.ws.Client;
import com.simplerobot.modules.utils.KQCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class VerbalTricksHandler implements IOnMessageEvent {
    @Override
    public int weight() {
        return 2;
    }
    private static Map<String, List<VerbalTricks>> cache = new ConcurrentHashMap<>();
    public static void setCache(Map<String, List<VerbalTricks>> cache){
        VerbalTricksHandler.cache = cache;
    }

    @Override
    public boolean onMessage(final Message message,final String command) {
        if(cache.size() == 0){
            return false;
        }
        // 群聊是否at了bot；是否私聊(私聊直接通过)
        boolean flag = false;
        if(MessageTypeEnum.privat.getType().equals(message.getMessage_type())){
            flag = true;
        }else if(MessageTypeEnum.group.getType().equals(message.getMessage_type())){
            if (CommonUtil.isAtSelf(message.getSelf_id(),command)) {
                flag = true;
            }
        }
        if(!flag){
            return false;
        }
        // 开始匹配
        String cmd = new String(command);
        cmd = cmd.replaceAll("\\[CQ:.*\\]", "").trim();

        List<VerbalTricks>  answerObj = null;
        for (Map.Entry<String, List<VerbalTricks>> item : this.cache.entrySet()) {
            if (cmd.matches(item.getKey())) {
                answerObj = item.getValue();
                break;
            }
        }
        if(answerObj == null){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new VerbalTricksHandler.Task(message,answerObj));
        return true;
    }

    public static class Task implements Runnable{
        private List<VerbalTricks>  answerObj;
        private Message message;

        public Task(Message message,List<VerbalTricks> answerObj){
            this.answerObj = answerObj;
            this.message = message;
        }


        @Override
        public void run() {
            if (answerObj.size() == 1) {
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),answerObj.get(0).getAnswer(), GocqActionEnum.SEND_MSG,true);
            }else{
                VerbalTricks verbalTricks = answerObj.get(CommonUtil.randomInt(0, answerObj.size() - 1));
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),verbalTricks.getAnswer(), GocqActionEnum.SEND_MSG,true);
            }
        }
    }
}