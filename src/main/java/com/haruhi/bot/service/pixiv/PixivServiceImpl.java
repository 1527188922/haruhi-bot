package com.haruhi.bot.service.pixiv;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.entity.Pixiv;
import com.haruhi.bot.mapper.PixivMapper;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.ws.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;


@Service
public class PixivServiceImpl extends ServiceImpl<PixivMapper, Pixiv> implements PixivService{

    @Autowired
    private PixivMapper pixivMapper;

    @Override
    public void roundSend(int num, Boolean isR18, List<String> tags, Message message,String tag) {
        List<Pixiv> pixivs = null;
        HashSet<Pixiv> pixivHashSet = null;
        boolean noTag = CollectionUtils.isEmpty(tags);
        if (noTag) {
            pixivs = pixivMapper.roundByTagLimit(num, isR18, null);
        } else {
            pixivs = pixivMapper.roundByTagsAll(isR18,tags);

        }
        if (CollectionUtils.isEmpty(pixivs)) {
            noDataSend(noTag,tag,message);
            return;
        }
        int size = pixivs.size();
        if(size > num){
            pixivHashSet = new HashSet<>(num);
            while (pixivHashSet.size() < num){
                pixivHashSet.add(pixivs.get(CommonUtil.randomInt(0,size - 1)));
            }
        }else if(size == num || size > 0){
            pixivHashSet = new HashSet<>(pixivs);
        }

        if (MessageEventEnum.group.getType().equals(message.getMessage_type())) {
            List<ForwardMsg> params = new ArrayList<>(pixivHashSet.size() + 1);
            params.add(CommonUtil.createForwardMsgItem(MessageFormat.format("tag：{0}\n※原图链接不需要翻墙，直接点",tag),message.getSelf_id(), BotConfig.NAME));
            if(pixivHashSet.size() > 0){
                groupSend(pixivHashSet,params,message);
            }else{
                groupSend(pixivs,params,message);
            }
        } else if (MessageEventEnum.privat.getType().equals(message.getMessage_type())) {
            if(pixivHashSet.size() > 0){
                privateSend(pixivHashSet,message);
            }else{
                privateSend(pixivs,message);
            }

        }
    }
    private void noDataSend(boolean noTag,String tag,Message message){
        if(noTag){
            Client.sendMessage(message.getUser_id(), message.getGroup_id(), message.getMessage_type(),"pix图库还没有图片~", GocqActionEnum.SEND_MSG, true);
        }else{
            Client.sendMessage(message.getUser_id(), message.getGroup_id(), message.getMessage_type(), MessageFormat.format("没有[{0}]的图片，换一个tag试试吧~", tag), GocqActionEnum.SEND_MSG, true);
        }
    }
    @Override
    public void privateSend(Collection<Pixiv> pixivs, Message message) {
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

