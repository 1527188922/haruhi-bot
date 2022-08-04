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
        Integer intPid;
        try {
            intPid = Integer.valueOf(pid.trim());
        }catch (Exception e){
            Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),"请输入正确的pid...", GocqActionEnum.SEND_MSG,true);
            return true;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new PixivByPidHandler.Task(pixivService,intPid,message));

        return true;
    }

    public static class Task implements Runnable{
        private PixivService pixivService;
        private Integer pid;
        private Message message;
        Task(PixivService pixivService,Integer pid,Message message){
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
                    groupSend(list,message);
                }else if(MessageTypeEnum.privat.getType().equals(message.getMessage_type())){
                    privateSend(list,message);
                }
            }else {
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), MessageFormat.format("没找到该pid[{0}]的图片。",pid), GocqActionEnum.SEND_MSG,true);
            }
        }
        private void privateSend(List<Pixiv> list,Message message){
            for (Pixiv pixiv : list) {
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), MessageFormat.format("标题：{0}\n作者：{1}\nuid：{2}\npid：{3}\nr18：{4}\n原图：{5}", pixiv.getTitle(), pixiv.getAuthor(),pixiv.getUid(), pixiv.getPid(), pixiv.getIsR18() ? "是" : "否", pixiv.getImgUrl()), GocqActionEnum.SEND_MSG,true);
            }
        }
        private void groupSend(List<Pixiv> list,Message message){
            List<ForwardMsg> params = new ArrayList<>();
            params.add(CommonUtil.createForwardMsgItem(MessageFormat.format("pid：{0}\n※原图链接不需要翻墙，直接点击",pid),message.getSelf_id(), BotConfig.NAME));
            for (Pixiv pixiv : list) {
                params.add(CommonUtil.createForwardMsgItem(MessageFormat.format("标题：{0}\n作者：{1}\nuid：{2}\npid：{3}\nr18：{4}\n原图：{5}", pixiv.getTitle(), pixiv.getAuthor(),pixiv.getUid(), pixiv.getPid(), pixiv.getIsR18() ? "是" : "否", pixiv.getImgUrl()),message.getSelf_id(), BotConfig.NAME));
            }
            Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG,message.getGroup_id(),params);
        }

    }


}
