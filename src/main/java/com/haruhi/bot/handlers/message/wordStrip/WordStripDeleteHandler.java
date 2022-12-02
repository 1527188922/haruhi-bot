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

@Slf4j
@Component
public class WordStripDeleteHandler implements IGroupMessageEvent {
    @Override
    public int weight() {
        return 94;
    }

    @Override
    public String funName() {
        return "删除词条";
    }

    private String keyWord;
    @Autowired
    private WordStripService wordStripService;


    public boolean matching(final String command) {
        if(command.startsWith(RegexEnum.WORD_STRIP_DELETE.getValue())){
            String keyWord = command.replaceFirst(RegexEnum.WORD_STRIP_DELETE.getValue(),"");
            if(Strings.isNotBlank(keyWord)){
                this.keyWord = keyWord;
                return true;
            }
        }
        this.keyWord = null;
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
            WordStrip one = wordStripService.getOne(queryWrapper);
            if(one == null){
                Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group, MessageFormat.format("词条不存在：{0}",this.keyWord), GocqActionEnum.SEND_MSG,true);
                return;
            }
            if(!one.getUserId().equals(message.getUserId())){
                Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group, MessageFormat.format("你不是该词条的创建人，不可删除：{0}",this.keyWord), GocqActionEnum.SEND_MSG,true);
                return;
            }
            try {
                if (wordStripService.removeById(one.getId())) {
                    WordStripHandler.cache.remove(message.getGroupId() + "-" + this.keyWord);
                    Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group, MessageFormat.format("删除词条成功：{0}",this.keyWord), GocqActionEnum.SEND_MSG,true);
                }
            }catch (Exception e){
                Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group, MessageFormat.format("删除词条异常：{0}",e.getMessage()), GocqActionEnum.SEND_MSG,true);
                log.error("删除词条异常",e);
            }

        });
        return true;
    }
}
