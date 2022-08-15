package com.haruhi.bot.handlers.message;

import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.utils.WordCloudUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class BulletChatWordCloudHandler implements IMessageEvent {

    @Override
    public int weight() {
        return 89;
    }
    @Override
    public boolean onMessage(final Message message,final String command) {
        if(!command.startsWith(RegexEnum.BULLET_CHAT_WORD_CLOUD.getValue())){
            return false;
        }
        String bv = command.replace(RegexEnum.BULLET_CHAT_WORD_CLOUD.getValue(), "");
        if(Strings.isBlank(bv)){
            return false;
        }
        if (!bv.startsWith("BV") && !bv.startsWith("bv")) {
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new BulletChatWordCloudHandler.Task(message,bv));
        return true;
    }

    public static class Task implements Runnable{

        private Message message;
        private String bv;
        public Task(Message message,String bv){
            this.message = message;
            this.bv = bv;
        }

        @Override
        public void run() {
            try {
                String cid = WordCloudUtil.getCid(bv);
                List<String> chatList = WordCloudUtil.getChatList(cid);
            }catch (Exception e){
                log.error("弹幕词云异常",e);
            }
        }
    }

}
