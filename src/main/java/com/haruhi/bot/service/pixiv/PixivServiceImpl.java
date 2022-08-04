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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class PixivServiceImpl extends ServiceImpl<PixivMapper, Pixiv> implements PixivService{

    @Autowired
    private PixivMapper pixivMapper;

    @Override
    public void roundSend(int num, Boolean isR18, String tag, Message message) {
        List<Pixiv> pixivs = null;
        if (CommonUtil.isBlank(tag)) {
            pixivs = pixivMapper.roundByTag(num, isR18, null);
        } else {
            pixivs = pixivMapper.roundByTag(num, isR18, tag.trim());
        }
        if (pixivs == null || pixivs.size() == 0) {
            Client.sendMessage(message.getUser_id(), message.getGroup_id(), message.getMessage_type(), MessageFormat.format("没有[{0}]的图片，换一个tag试试吧~", tag), GocqActionEnum.SEND_MSG, true);
            return;
        }

        if (MessageTypeEnum.group.getType().equals(message.getMessage_type())) {
            List<ForwardMsg> params = new ArrayList<>();
            params.add(CommonUtil.createForwardMsgItem(MessageFormat.format("tag：{0}\n※原图链接不需要翻墙，直接点",tag),message.getSelf_id(), BotConfig.NAME));
            for (Pixiv pixiv : pixivs) {
                params.add(CommonUtil.createForwardMsgItem(MessageFormat.format("标题：{0}\n作者：{1}\nuid：{2}\npid：{3}\nr18：{4}\n原图：{5}", pixiv.getTitle(), pixiv.getAuthor(),pixiv.getUid(), pixiv.getPid(), pixiv.getIsR18() ? "是" : "否", pixiv.getImgUrl()),message.getSelf_id(),BotConfig.NAME));
            }
            Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG, message.getGroup_id(), params);
        } else if (MessageTypeEnum.privat.getType().equals(message.getMessage_type())) {
            for (Pixiv pixiv : pixivs) {
                Client.sendMessage(message.getUser_id(), message.getGroup_id(), message.getMessage_type(), MessageFormat.format("标题：{0}\n作者：{1}\nuid：{2}\npid：{3}\nr18：{4}\n原图：{5}", pixiv.getTitle(), pixiv.getAuthor(),pixiv.getUid(), String.valueOf(pixiv.getPid()), pixiv.getIsR18() ? "是" : "否", pixiv.getImgUrl()), GocqActionEnum.SEND_MSG, false);
            }
        }
    }
}

