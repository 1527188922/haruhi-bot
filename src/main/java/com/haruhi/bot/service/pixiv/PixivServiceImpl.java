package com.haruhi.bot.service.pixiv;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.dto.gocq.response.ForwardMsg;
import com.haruhi.bot.entity.Pixiv;
import com.haruhi.bot.mapper.PixivMapper;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.ws.Client;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

@Service
public class PixivServiceImpl extends ServiceImpl<PixivMapper, Pixiv> implements PixivService{

    @Autowired
    private PixivMapper pixivMapper;

    @Override
    public void roundSend(int num, Boolean isR18, String tag, Message message) {
        List<Pixiv> pixivs = null;
        HashSet<Pixiv> pixivHashSet = null;
        if (Strings.isBlank(tag)) {
            pixivs = pixivMapper.roundByTagLimit(num, isR18, null);
        } else {
            pixivs = pixivMapper.roundByTagAll(isR18, tag.trim());
            if (pixivs == null || pixivs.size() == 0) {
                Client.sendMessage(message.getUser_id(), message.getGroup_id(), message.getMessage_type(), MessageFormat.format("没有[{0}]的图片，换一个tag试试吧~", tag), GocqActionEnum.SEND_MSG, true);
                return;
            }
            int size = pixivs.size();
            if(size > num){
                pixivHashSet = new HashSet<>();
                while (pixivHashSet.size() < num){
                    pixivHashSet.add(pixivs.get(CommonUtil.randomInt(0,size - 1)));
                }
            }
        }

        if (MessageTypeEnum.group.getType().equals(message.getMessage_type())) {
            List<ForwardMsg> params = new ArrayList<>();
            params.add(CommonUtil.createForwardMsgItem(MessageFormat.format("tag：{0}\n※原图链接不需要翻墙，直接点",tag),message.getSelf_id(), BotConfig.NAME));
            if(pixivHashSet != null && pixivHashSet.size() > 0){
                groupSend(pixivHashSet,params,message);
            }else{
                groupSend(pixivs,params,message);
            }
        } else if (MessageTypeEnum.privat.getType().equals(message.getMessage_type())) {
            if(pixivHashSet != null && pixivHashSet.size() > 0){
                privateSend(pixivHashSet,message);
            }else{
                privateSend(pixivs,message);
            }

        }
    }

    @Override
    public void privateSend(Collection<Pixiv> pixivs,Message message) {
        for (Pixiv pixiv : pixivs) {
            Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), MessageFormat.format("标题：{0}\n作者：{1}\nuid：{2}\npid：{3}\nr18：{4}\n原图：{5}", pixiv.getTitle(), pixiv.getAuthor(),pixiv.getUid(), pixiv.getPid(), pixiv.getIsR18() ? "是" : "否", pixiv.getImgUrl()), GocqActionEnum.SEND_MSG,true);
        }
    }

    @Override
    public void groupSend(Collection<Pixiv> pixivs, List<ForwardMsg> forwardMsgs, Message message) {
        for (Pixiv pixiv : pixivs) {
            forwardMsgs.add(CommonUtil.createForwardMsgItem(MessageFormat.format("标题：{0}\n作者：{1}\nuid：{2}\npid：{3}\nr18：{4}\n原图：{5}", pixiv.getTitle(), pixiv.getAuthor(),pixiv.getUid(), pixiv.getPid(), pixiv.getIsR18() ? "是" : "否", pixiv.getImgUrl()),message.getSelf_id(), BotConfig.NAME));
        }
        Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG,message.getGroup_id(),forwardMsgs);
    }
}

