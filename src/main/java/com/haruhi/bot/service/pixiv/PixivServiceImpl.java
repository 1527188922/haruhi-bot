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
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j
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
            empty(noTag,tag,message);
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

        if (MessageEventEnum.group.getType().equals(message.getMessageType())) {
            List<ForwardMsg> params = new ArrayList<>(pixivHashSet.size() + 1);
            params.add(CommonUtil.createForwardMsgItem(MessageFormat.format("tag：{0}\n※原图链接不需要翻墙，直接点",tag),message.getSelfId(), BotConfig.NAME));
            if(pixivHashSet.size() > 0){
                sendGroup(pixivHashSet,params,message);
            }else{
                sendGroup(pixivs,params,message);
            }
        } else if (MessageEventEnum.privat.getType().equals(message.getMessageType())) {
            if(pixivHashSet.size() > 0){
                sendPrivate(pixivHashSet,message);
            }else{
                sendPrivate(pixivs,message);
            }

        }
    }
    private void empty(boolean noTag, String tag, Message message){
        if(noTag){
            Client.sendMessage(message.getUserId(), message.getGroupId(), message.getMessageType(),"pix图库还没有图片~", GocqActionEnum.SEND_MSG, true);
        }else{
            Client.sendMessage(message.getUserId(), message.getGroupId(), message.getMessageType(), MessageFormat.format("没有[{0}]的图片，换一个tag试试吧~", tag), GocqActionEnum.SEND_MSG, true);
        }
    }
    private List<ForwardMsg> createForwardMessage(Collection<Pixiv> pixivs,List<ForwardMsg> forwardMsgs,Message message){
        if (forwardMsgs == null) {
            forwardMsgs = new ArrayList<>();
        }
        for (Pixiv pixiv : pixivs) {
            forwardMsgs.add(CommonUtil.createForwardMsgItem(MessageFormat.format("标题：{0}\n作者：{1}\nuid：{2}\npid：{3}\nr18：{4}\n原图：{5}", pixiv.getTitle(), pixiv.getAuthor(),pixiv.getUid(), pixiv.getPid(), pixiv.getIsR18() ? "是" : "否", pixiv.getImgUrl()),message.getSelfId(), BotConfig.NAME));
            forwardMsgs.add(CommonUtil.createForwardMsgItem(MessageFormat.format("标题：{0}\n作者：{1}\nuid：{2}\npid：{3}\nr18：{4}\n原图：{5}\n原图：{6}", pixiv.getTitle(), pixiv.getAuthor(),pixiv.getUid(), pixiv.getPid(), pixiv.getIsR18() ? "是" : "否", pixiv.getImgUrl(),"https://pixiv.re/" + appendImageP(pixiv.getPid(), pixiv.getImgUrl()) + ".jpg"),message.getSelf_id(), BotConfig.NAME));
        }
        return forwardMsgs;
    }
    @Override
    public void sendPrivate(Collection<Pixiv> pixivs, Message message) {
        List<ForwardMsg> forwardMessage = createForwardMessage(pixivs, null, message);
        Client.sendMessage(GocqActionEnum.SEND_PRIVATE_FORWARD_MSG,message.getUserId(),forwardMessage);
    }

    @Override
    public void sendGroup(Collection<Pixiv> pixivs, List<ForwardMsg> forwardMsgs, Message message) {
        List<ForwardMsg> forwardMessage = createForwardMessage(pixivs, forwardMsgs, message);
        Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG,message.getGroupId(),forwardMessage);
    }
    private static Pattern compile = Pattern.compile("_p(.*?)(.jpg|.png)");
    private String appendImageP(String pid,String imageUrl){
        if(Strings.isBlank(imageUrl)){
            return pid;
        }
        int p;
        Matcher matcher = compile.matcher(imageUrl);
        if (matcher.find()) {
            String s = matcher.group(1);
            try {
                p = Integer.parseInt(s) + 1;
                if(p == 1){
                    return pid;
                }

                return pid + "-" + p;
            }catch (Exception e){
                log.error("追加imagep异常,imagep:{}",imageUrl,e);
                return pid;
            }
        }
        return pid;
    }
}

