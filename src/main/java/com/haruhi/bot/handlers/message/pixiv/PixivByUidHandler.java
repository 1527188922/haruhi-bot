package com.haruhi.bot.handlers.message.pixiv;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.dto.gocq.response.ForwardMsg;
import com.haruhi.bot.entity.Pixiv;
import com.haruhi.bot.event.message.IOnMessageEvent;
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

@Slf4j
@Component
public class PixivByUidHandler implements IOnMessageEvent {
    @Override
    public int weight() {
        return 103;
    }
    @Autowired
    private PixivService pixivService;

    @Override
    public boolean onMessage(Message message, String command) {
        String[] split = RegexEnum.PIXIV_UID.getValue().split("\\|");
        String uid = "";
        for (String s : split) {
            if (command.startsWith(s)) {
                uid = command.replace(s,"");
                break;
            }
        }
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
            Set<Pixiv> pixivSet = new HashSet<>();
            if(size > 20){
                while (pixivSet.size() < 20){
                    pixivSet.add(list.get(CommonUtil.randomInt(0,size - 1)));
                }
            }
            if(MessageTypeEnum.group.getType().equals(message.getMessage_type())){
                ArrayList<ForwardMsg> params = new ArrayList<>();
                params.add(CommonUtil.createForwardMsgItem(MessageFormat.format("uid：{0}\n※原图链接不需要翻墙，直接点",uid),message.getSelf_id(), BotConfig.NAME));
                pixivService.groupSend(pixivSet,params,message);
            }else if(MessageTypeEnum.privat.getType().equals(message.getMessage_type())){
                pixivService.privateSend(pixivSet,message);
            }
        }
    }
}