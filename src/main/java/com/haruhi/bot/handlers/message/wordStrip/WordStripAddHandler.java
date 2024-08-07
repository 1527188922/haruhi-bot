package com.haruhi.bot.handlers.message.wordStrip;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.entity.WordStrip;
import com.haruhi.bot.event.message.IGroupMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.wordStrip.WordStripService;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class WordStripAddHandler implements IGroupMessageEvent {
    @Override
    public int weight() {
        return 96;
    }

    @Override
    public String funName() {
        return "添加词条";
    }

    private String keyWord;
    private String answer;

    @Autowired
    private WordStripService wordStripService;

    public boolean matching(final String command) {
        Pattern compile = Pattern.compile(RegexEnum.WORD_STRIP_ADD.getValue());
        Matcher matcher = compile.matcher(command);
        if(matcher.find()){
            String keyWord = matcher.group(1);
            if(Strings.isNotBlank(keyWord)){
                String answer = command.substring(command.indexOf("答") + 1);
                if(Strings.isNotBlank(answer)){
                    this.keyWord = keyWord;
                    this.answer = answer;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onGroup(final Message message,final String command) {
        if (!matching(command)) {
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            LambdaQueryWrapper<WordStrip> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WordStrip::getGroupId,message.getGroupId()).eq(WordStrip::getKeyWord,this.keyWord);

            try {
                WordStrip wordStrip = wordStripService.getOne(queryWrapper);
                WordStrip param = new WordStrip();
                boolean save = false;
                if(wordStrip != null){
                    if(wordStrip.getUserId().equals(message.getUserId())){
                        param.setAnswer(this.answer);
                        save = wordStripService.update(param,queryWrapper);
                    }else {
                        Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group,MessageFormat.format("已存在词条：{0}",this.keyWord), GocqActionEnum.SEND_MSG,true);
                        return;
                    }
                }else{
                    param.setKeyWord(this.keyWord);
                    param.setAnswer(this.answer);
                    param.setGroupId(message.getGroupId());
                    param.setUserId(message.getUserId());
                    save = wordStripService.save(param);
                }
                if(save){
                    WordStripHandler.cache.put(message.getGroupId() + "-" + this.keyWord,this.answer);
                    Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group,MessageFormat.format("词条添加成功：{0}",this.keyWord), GocqActionEnum.SEND_MSG,true);
                    return;
                }
                Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group, MessageFormat.format("词条添加失败：{0}-->0",this.keyWord), GocqActionEnum.SEND_MSG,true);
            }catch (Exception e){
                log.error("添加词条异常",e);
            }

        });
        return true;
    }
}
