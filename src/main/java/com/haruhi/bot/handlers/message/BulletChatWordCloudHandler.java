package com.haruhi.bot.handlers.message;

import com.haruhi.bot.config.env.IEnvPathConfig;
import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.xml.bilibili.PlayerInfoResp;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.thread.WordSlicesTask;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.FileUtil;
import com.haruhi.bot.utils.WordCloudUtil;
import com.haruhi.bot.ws.Client;
import com.simplerobot.modules.utils.KQCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BulletChatWordCloudHandler implements IMessageEvent {

    @Override
    public int weight() {
        return 89;
    }

    @Override
    public String funName() {
        return "弹幕词云";
    }

    @Autowired
    private IEnvPathConfig envConfig;
    private static String basePath;
    @PostConstruct
    private void mkdirs(){
        basePath = envConfig.resourcesImagePath() + File.separator + "bulletWordCloud";
        File file = new File(basePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    @Override
    public boolean onMessage(final Message message,final String command) {
        if(!command.startsWith(RegexEnum.BULLET_CHAT_WORD_CLOUD.getValue())){
            return false;
        }
        String param = command.replaceFirst(RegexEnum.BULLET_CHAT_WORD_CLOUD.getValue(), "");
        if(Strings.isBlank(param)){
            return false;
        }

        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new BulletChatWordCloudHandler.Task(message,param));
        return true;
    }
    private static String getBv(String param){
        String bv = null;
        if(param.startsWith("av") || param.startsWith("AV")){
            bv = WordCloudUtil.getBvByAv(param);
        }else if(param.startsWith("bv") || param.startsWith("BV")){
            bv = param;
        }
        return bv;
    }
    public static class Task implements Runnable{

        private Message message;
        private String param;
        public Task(Message message,String param){
            this.message = message;
            this.param = param;
        }

        @Override
        public void run() {
            String outPutPath = null;
            try {
                String bv = getBv(param);
                if(Strings.isBlank(bv)){
                    log.error("bv号获取失败");
                    return;
                }
                PlayerInfoResp playerInfoResp = WordCloudUtil.getPlayerInfo(bv);
                if(playerInfoResp == null || Strings.isBlank(playerInfoResp.getCid())){
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), "视频cid获取失败", GocqActionEnum.SEND_MSG,true);
                    return;
                }
                List<String> chatList = WordCloudUtil.getChatList(playerInfoResp.getCid());
                if(chatList == null || chatList.size() == 0){
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), "弹幕数为0，不生成", GocqActionEnum.SEND_MSG,true);
                    return;
                }
                KQCodeUtils instance = KQCodeUtils.getInstance();
                String cq = "";
                if(Strings.isNotBlank(playerInfoResp.getFirst_frame())){
                    cq = instance.toCq(CqCodeTypeEnum.image.getType(), "url=" + playerInfoResp.getFirst_frame(),"file="+CommonUtil.uuid()+".jpg");
                    cq = "\n"+cq;
                }
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), MessageFormat.format("获取弹幕成功，数量：{0}\n开始生成...\n标题：{1}{2}",chatList.size(),playerInfoResp.getPart(),cq), GocqActionEnum.SEND_MSG,false);
                List<String> list = WordSlicesTask.execute(chatList);
                if(list == null || list.size() == 0){
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), "有效词料为0，不生成", GocqActionEnum.SEND_MSG,true);
                }
                Map<String, Integer> map = WordCloudUtil.setFrequency(list);
                String fileName = bv + "-" + CommonUtil.uuid() + ".png";
                outPutPath = basePath + File.separator + fileName;
                WordCloudUtil.generateWordCloudImage(map,outPutPath);

                String imageCq = instance.toCq(CqCodeTypeEnum.image.getType(), "file=file:///" + outPutPath);
                Client.sendRestMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),imageCq, GocqActionEnum.SEND_MSG,false);
            }catch (Exception e){
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), MessageFormat.format("弹幕词云生成异常:{0}",e.getMessage()), GocqActionEnum.SEND_MSG,true);
                log.error("弹幕词云异常",e);
            }finally {
                FileUtil.deleteFile(outPutPath);
            }
        }
    }

}
