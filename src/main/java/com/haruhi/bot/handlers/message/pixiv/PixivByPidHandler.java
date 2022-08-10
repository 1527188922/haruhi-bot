package com.haruhi.bot.handlers.message.pixiv;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
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
import java.util.List;

@Slf4j
@Component
public class PixivByPidHandler implements IOnMessageEvent {
    @Override
    public int weight() {
        return 92;
    }
    @Autowired
    private PixivService pixivService;

    @Override
    public boolean onMessage(Message message, String command) {
        String[] split = RegexEnum.PIXIV_PID.getValue().split("\\|");
        String pid = null;
        for (String s : split) {
            if (command.startsWith(s)) {
                pid = command.replace(s,"");
                break;
            }
        }
        if(Strings.isBlank(pid)){
            return false;
        }
        String trim = "";
        try {
            trim = pid.trim();
            Integer.valueOf(trim);
        }catch (Exception e){
            Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),"请输入正确的pid...", GocqActionEnum.SEND_MSG,true);
            return true;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new PixivByPidHandler.Task(pixivService,trim,message));

        return true;
    }

    public static class Task implements Runnable{
        private PixivService pixivService;
        private String pid;
        private Message message;
        Task(PixivService pixivService,String pid,Message message){
            this.pixivService = pixivService;
            this.pid = pid;
            this.message = message;
        }
        @Override
        public void run() {
            LambdaQueryWrapper<Pixiv> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Pixiv::getPid,pid);
            List<Pixiv> list = pixivService.list(queryWrapper);
            if(list != null && list.size() > 0){
                if(MessageTypeEnum.group.getType().equals(message.getMessage_type())){
                    List<ForwardMsg> params = new ArrayList<>();
                    params.add(CommonUtil.createForwardMsgItem(MessageFormat.format("pid：{0}\n※原图链接不需要翻墙，直接点击",pid),message.getSelf_id(), BotConfig.NAME));
                    pixivService.groupSend(list,params,message);
                }else if(MessageTypeEnum.privat.getType().equals(message.getMessage_type())){
                    pixivService.privateSend(list,message);
                }
            }else {
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), MessageFormat.format("没找到该pid[{0}]的图片。",pid), GocqActionEnum.SEND_MSG,true);
            }
        }

    }


}
