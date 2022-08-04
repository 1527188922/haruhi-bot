package com.haruhi.bot.handlers.message.pixiv;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.entity.Pixiv;
import com.haruhi.bot.event.message.IOnMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.pixiv.PixivService;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Slf4j
@Component
public class PixivCountHandler implements IOnMessageEvent {
    @Override
    public int weight() {
        return 101;
    }

    @Autowired
    private PixivService pixivService;

    @Override
    public boolean onMessage(final Message message,final String command) {
        if(!command.matches(RegexEnum.PIXIV_COUNT.getValue())){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            QueryWrapper<Pixiv> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Pixiv::getIsR18,false);
            QueryWrapper<Pixiv> queryWrapperR18 = new QueryWrapper<>();
            queryWrapperR18.lambda().eq(Pixiv::getIsR18,true);
            int count = pixivService.count(queryWrapper);
            int countR18 = pixivService.count(queryWrapperR18);
            Client.sendMessage(message.getUser_id(), message.getGroup_id(),message.getMessage_type(), MessageFormat.format("pixiv库：\n非r18：{0}\nr18：{1}\n总计：{2}",count,countR18,count + countR18), GocqActionEnum.SEND_MSG,true);
        });

        return true;
    }
}
