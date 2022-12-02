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
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PixivByPidHandler implements IMessageEvent {
    @Override
    public int weight() {
        return 92;
    }

    @Override
    public String funName() {
        return "p站图片根据pid";
    }

    @Autowired
    private PixivService pixivService;

    @Override
    public boolean onMessage(final Message message,final String command) {
        String pid = CommonUtil.commandReplaceFirst(command,RegexEnum.PIXIV_PID);

        if(Strings.isBlank(pid)){
            return false;
        }
        String trim = "";
        try {
            trim = pid.trim();
            Integer.valueOf(trim);
        }catch (Exception e){
            Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(),"请输入正确的pid...", GocqActionEnum.SEND_MSG,true);
            return true;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new Task(pixivService,trim,message));

        return true;
    }

    public static String u = "https://pixiv.re/";
    private class Task implements Runnable{
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
            if(!CollectionUtils.isEmpty(list)){
                if(MessageEventEnum.group.getType().equals(message.getMessageType())){
                    List<ForwardMsg> params = new ArrayList<>(list.size() + 1);
                    params.add(CommonUtil.createForwardMsgItem(MessageFormat.format("pid：{0}\n※原图链接不需要翻墙，直接点击",pid),message.getSelfId(), BotConfig.NAME));
                    pixivService.sendGroup(list,params,message);
                }else if(MessageEventEnum.privat.getType().equals(message.getMessageType())){
                    pixivService.sendPrivate(list,message);
                }
            }else {
                String temp = "{0}{1}-{2}.jpg";
                String noImageTip = "本地库没有pid[{0}]的图片\n试试点击下面这些链接吧\n※如果pid只有1p,只需要点第一个链接,后缀带有数字的链接,多p情况下有效";

                ArrayList<ForwardMsg> params = new ArrayList<>(21);
                params.add(CommonUtil.createForwardMsgItem(MessageFormat.format(noImageTip,pid),message.getSelfId(),BotConfig.NAME));
                params.add(CommonUtil.createForwardMsgItem(MessageFormat.format("{0}{1}.jpg",u,pid),message.getSelfId(),BotConfig.NAME));
                for (int i = 2; i <= 20; i++) {
                    params.add(CommonUtil.createForwardMsgItem(MessageFormat.format(temp,u,pid,i),message.getSelfId(),BotConfig.NAME));
                }

                if(MessageEventEnum.group.getType().equals(message.getMessageType())){
                    Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG,message.getGroupId(),params);
                }else if(MessageEventEnum.privat.getType().equals(message.getMessageType())){
                    Client.sendMessage(GocqActionEnum.SEND_PRIVATE_FORWARD_MSG,message.getUserId(),params);
                }
            }
        }

    }


}
