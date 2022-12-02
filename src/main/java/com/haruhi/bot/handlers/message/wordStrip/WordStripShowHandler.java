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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.List;

@Slf4j
@Component
public class WordStripShowHandler implements IGroupMessageEvent {
    @Override
    public int weight() {
        return 93;
    }

    @Override
    public String funName() {
        return "本群词条";
    }

    @Autowired
    private WordStripService wordStripService;
    public boolean matching(final String command) {
        return command.matches(RegexEnum.WORD_STRIP_SHOW.getValue());
    }

    @Override
    public boolean onGroup(final Message message,final String command) {
        if (!matching(command)) {
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            LambdaQueryWrapper<WordStrip> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WordStrip::getGroupId,message.getGroupId());
            List<WordStrip> list = wordStripService.list(queryWrapper);
            if(CollectionUtils.isEmpty(list)){
                Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group,"本群没有词条", GocqActionEnum.SEND_MSG,true);
                return;
            }
            Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group, processWordStrip(list), GocqActionEnum.SEND_MSG,false);
        });
        return true;
    }

    private String processWordStrip(List<WordStrip> list){
        StringBuilder stringBuilder = new StringBuilder("本群词条：\n");
        for (WordStrip wordStrip : list) {
            stringBuilder.append(MessageFormat.format("[{0}]-[{1}] 创建人：{2}\n",wordStrip.getKeyWord(),wordStrip.getAnswer(),wordStrip.getUserId()));
        }
        return stringBuilder.toString();
    }
}
