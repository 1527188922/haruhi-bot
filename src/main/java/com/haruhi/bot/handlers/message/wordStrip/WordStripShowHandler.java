package com.haruhi.bot.handlers.message.wordStrip;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.entity.WordStrip;
import com.haruhi.bot.event.message.IOnGroupMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.wordStrip.WordStripService;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;

@Slf4j
@Component
public class WordStripShowHandler implements IOnGroupMessageEvent {
    @Override
    public int weight() {
        return 93;
    }

    @Autowired
    private WordStripService wordStripService;
    public boolean matches(final String command) {
        return command.matches(RegexEnum.WORD_STRIP_SHOW.getValue());
    }

    @Override
    public boolean onGroup(Message message, String command) {
        if (!matches(command)) {
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            LambdaQueryWrapper<WordStrip> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WordStrip::getGroupId,message.getGroup_id());
            List<WordStrip> list = wordStripService.list(queryWrapper);
            if(list == null || list.size() == 0){
                Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageTypeEnum.group,"本群没有词条", GocqActionEnum.SEND_MSG,true);
                return;
            }
            Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageTypeEnum.group, processWordStrip(list), GocqActionEnum.SEND_MSG,false);
        });
        return true;
    }

    private String processWordStrip(List<WordStrip> list){
        StringBuffer stringBuffer = new StringBuffer("本群词条：\n");
        for (WordStrip wordStrip : list) {
            stringBuffer.append(MessageFormat.format("[{0}]-[{1}] 创建人：{2}\n",wordStrip.getKeyWord(),wordStrip.getAnswer(),wordStrip.getUserId()));
        }
        return stringBuffer.toString();
    }
}