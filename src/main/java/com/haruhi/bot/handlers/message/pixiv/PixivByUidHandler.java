package com.haruhi.bot.handlers.message.pixiv;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.entity.Pixiv;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.pixiv.PixivService;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PixivByUidHandler implements IMessageEvent {
    @Override
    public int weight() {
        return 103;
    }

    @Override
    public String funName() {
        return "p站图片根据uid";
    }

    @Autowired
    private PixivService pixivService;

    @Override
    public boolean onMessage(final Message message,final String command) {
        String uid = CommonUtil.commandReplaceFirst(command,RegexEnum.PIXIV_UID);
        if(Strings.isBlank(uid)){
            return false;
        }
        String uidTrim = "";
        try {
            uidTrim = uid.trim();
            Integer.valueOf(uidTrim);
        }catch (Exception e){
            Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),"请输入正确的uid...", GocqActionEnum.SEND_MSG,true);
            return true;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new PixivByUidHandler.Task(pixivService,uidTrim,message));

        return false;
    }
    public static class Task implements Runnable{
        private PixivService pixivService;
        private String uid;
        private Message message;
        public Task(PixivService pixivService,String uid,Message message){
            this.pixivService = pixivService;
            this.uid = uid;
            this.message = message;
        }
        @Override
        public void run() {
            LambdaQueryWrapper<Pixiv> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Pixiv::getUid,uid);
            List<Pixiv> list = pixivService.list(queryWrapper);
            if(list == null || list.size() == 0){
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), MessageFormat.format("没有uid[{0}]的图片...",uid), GocqActionEnum.SEND_MSG,true);
                return;
            }
            int size = list.size();
            Set<Pixiv> pixivSet;
            if(size > 20){
                pixivSet = new HashSet<>();
                while (pixivSet.size() < 20){
                    pixivSet.add(list.get(CommonUtil.randomInt(0,size - 1)));
                }
            }else{
                pixivSet = list.stream().collect(Collectors.toSet());
            }
            if(MessageEventEnum.group.getType().equals(message.getMessage_type())){
                ArrayList<ForwardMsg> params = new ArrayList<>(pixivSet.size() + 1);
                params.add(CommonUtil.createForwardMsgItem(MessageFormat.format("uid：{0}\n※原图链接不需要翻墙，直接点",uid),message.getSelf_id(), BotConfig.NAME));
                pixivService.groupSend(pixivSet,params,message);
            }else if(MessageEventEnum.privat.getType().equals(message.getMessage_type())){
                pixivService.privateSend(pixivSet,message);
            }
        }
    }
}
