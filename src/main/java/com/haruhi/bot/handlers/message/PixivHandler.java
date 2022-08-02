package com.haruhi.bot.handlers.message;

import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.entity.Pixiv;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.event.message.IOnMessageEvent;
import com.haruhi.bot.service.pixiv.PixivService;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.ws.Client;
import com.simplerobot.modules.utils.KQCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PixivHandler implements IOnMessageEvent {
    @Override
    public int weight() {
        return 97;
    }

    @Autowired
    private PixivService pixivService;

    private String tag;

    @Override
    public synchronized boolean matches(final Message message,final String command,final AtomicInteger total) {
        if(total.get() == 0){
            KQCodeUtils instance = KQCodeUtils.getInstance();
            String cq = instance.getCq(command, 0);
            if(cq != null){
                after();
                return false;
            }
            String[] split = RegexEnum.PIXIV.getValue().split("\\|");
            for (String s : split) {
                if (command.startsWith(s)) {
                    tag = command.replace(s,"");
                    return true;
                }
            }
        }
        after();
        return false;
    }

    @Override
    public void onMessage(Message message, String command) {
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new PixivTask(pixivService,tag,message));
    }
    private void after(){
        this.tag = null;
    }

    public static class PixivTask implements Runnable{
        private PixivService pixivService;
        private String tag;
        private Message message;
        public PixivTask(PixivService pixivService, String tag, Message message){
            // 重新new 一个字符串，防止后续tag发生改变会影响线程内的tag
            this.tag = new String(tag == null ? "" : tag);
            this.pixivService = pixivService;
            this.message = message;
        }
        private static String proxy = "i.pixiv.re";
        private static String host = "i.pximg.net";

        @Override
        public void run() {
            Pixiv pixiv = null;
            if (CommonUtil.isBlank(this.tag)) {
                pixiv = pixivService.roundOneByTag(false,null);
            }else{
                pixiv = pixivService.roundOneByTag(false,tag.trim());
            }
            if(pixiv == null){
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), MessageFormat.format("没有[{0}]的图片，换一个tag试试吧~",this.tag), GocqActionEnum.SEND_MSG,true);
                return;
            }
            proxyUrl(pixiv);
            KQCodeUtils instance = KQCodeUtils.getInstance();
            String cqImage = instance.toCq(CqCodeTypeEnum.image.getType(), "file=" + pixiv.getImgUrl(),"url=" + pixiv.getImgUrl());
            Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),MessageFormat.format("标题：{0}\n作者：{1}\npid：{2}\n原图：{3}\n{4}\n{5}",pixiv.getTitle(),pixiv.getAuthor(),String.valueOf(pixiv.getPid()),pixiv.getImgUrl(),cqImage,"※↑若文件很小，可能是略缩图，原画质请点击原图链接"), GocqActionEnum.SEND_MSG,false);
        }
        private void proxyUrl(Pixiv pixiv){
            pixiv.setImgUrl(pixiv.getImgUrl().replace(host,proxy));
        }
    }


}
