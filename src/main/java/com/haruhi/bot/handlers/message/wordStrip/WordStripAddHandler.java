package com.haruhi.bot.handlers.message.wordStrip;

import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.entity.WordStrip;
import com.haruhi.bot.event.message.IOnGroupMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.wordStrip.WordStripService;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class WordStripAddHandler implements IOnGroupMessageEvent {
    @Override
    public int weight() {
        return 96;
    }
    private String keyWord;
    private String answer;

    @Autowired
    private WordStripService wordStripService;

    @Override
    public boolean matches(final Message message,final String command,final AtomicInteger total) {
        synchronized (total){
            Pattern compile = Pattern.compile(RegexEnum.WORD_STRIP_ADD.getValue());
            Matcher matcher = compile.matcher(command);
            if(matcher.find()){
                String keyWord = matcher.group(1);
                if(keyWord != null && !CommonUtil.isBlank(keyWord)){
                    String answer = command.substring(command.indexOf("答") + 1);
                    if(answer != null && !CommonUtil.isBlank(answer)){
                        this.keyWord = keyWord;
                        this.answer = answer;
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public void onGroup(Message message, String command) {
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            try {
                WordStrip param = new WordStrip();
                param.setKeyWord(this.keyWord);
                param.setAnswer(this.answer);
                param.setGroupId(message.getGroup_id());
                param.setUserId(message.getUser_id());
                if(wordStripService.save(param)){
                    WordStripHandler.cache.put(message.getGroup_id()+"-"+this.keyWord,this.answer);
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageTypeEnum.group,MessageFormat.format("词条添加成功：{0}",this.keyWord), GocqActionEnum.SEND_MSG,true);
                    return;
                }
                Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageTypeEnum.group, MessageFormat.format("词条添加失败：{0}-->0",this.keyWord), GocqActionEnum.SEND_MSG,true);
            }catch (Exception e){
                log.error("添加词条异常",e);
            }

        });
    }
}
