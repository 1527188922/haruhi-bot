package com.haruhi.bot.handlers.message;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.cache.CacheSet;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.ThirdPartyURL;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.HttpResponse;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.dto.searchImage.response.Results;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.handlers.message.pixiv.PixivByPidHandler;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.GocqRequestUtil;
import com.haruhi.bot.utils.RestUtil;
import com.haruhi.bot.ws.Client;
import com.simplerobot.modules.utils.KQCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SearchImageHandler implements IMessageEvent {

    @Override
    public int weight() {
        return 98;
    }

    @Override
    public String funName() {
        return "识图";
    }
    private static int size = 20;

    private static CacheSet<String> cache = new CacheSet<>(30,TimeUnit.SECONDS,size);

    private String getKey(String userId,String groupId){
        return userId + "-" + groupId;
    }
    @Override
    public boolean onMessage(final Message message,final String command) {
        String cq = replySearch(message);
        String key = null;
        if(Strings.isNotBlank(cq)){
            // 回复式识图
            startSearch(message,cq,null);
            return true;
        }else{
            KQCodeUtils utils = KQCodeUtils.getInstance();
            cq = utils.getCq(command, CqCodeTypeEnum.image.getType(), 0);
            key = getKey(message.getUserId(), message.getGroupId());
            boolean matches = false;
            if(cache.contains(key) && cq != null){
                // 存在缓存 并且 图片cq码不为空
                startSearch(message,cq,key);
            }else{
                String[] split = RegexEnum.SEARCH_IMAGE.getValue().split("\\|");
                for (String s : split) {
                    if(command.startsWith(s)){
                        matches = true;
                        break;
                    }
                }
                if(matches && cq == null){
                    cache.add(key);
                    Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(),"图呢！", GocqActionEnum.SEND_MSG,true);
                    return true;
                }else if(matches){
                    startSearch(message,cq,key);
                    return true;
                }
            }
        }
        return false;
    }

    private void startSearch(Message message, String cq, String key){
        Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(),"开始搜图...",GocqActionEnum.SEND_MSG,true);
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new SearchImageTask(message,cq));
        if(key != null){
            cache.remove(key);
        }
    }

    private String replySearch(final Message message){
        if (MessageEventEnum.group.getType().equals(message.getMessageType())) {
            KQCodeUtils instance = KQCodeUtils.getInstance();
            String s = message.getMessage().replaceAll(RegexEnum.CQ_CODE_REPLACR.getValue(), "").trim();
            if (s.matches(RegexEnum.SEARCH_IMAGE.getValue())) {
                String cq = instance.getCq(message.getMessage(), CqCodeTypeEnum.reply.getType());
                if(Strings.isNotBlank(cq)){
                    String messageId = instance.getParam(cq, "id");
                    Message msg = GocqRequestUtil.getMsg(messageId);
                    String respMessage = msg.getMessage();
                    String cq1 = instance.getCq(respMessage, CqCodeTypeEnum.image.getType());
                    return cq1;
                }
            }
        }
        return null;
    }

    private class SearchImageTask implements Runnable{
        private Message message;
        private String cq;

        SearchImageTask(Message message, String cq){
            this.message = message;
            this.cq = cq;
        }
        @Override
        public void run() {
            KQCodeUtils instance = KQCodeUtils.getInstance();
            String imageUrl = instance.getParam(this.cq, "url",CqCodeTypeEnum.image.getType(),0);

            LinkedMultiValueMap<String,Object> param = new LinkedMultiValueMap<>(6);
            param.add("output_type",2);
            param.add("api_key",BotConfig.SEARCH_IMAGE_KEY);
            param.add("testmode",1);
            param.add("numres",6);
            param.add("db",99);
            param.add("url",imageUrl);
            try {
                log.info("开始请求搜图接口,图片:{}",imageUrl);
                String response = RestUtil.sendPostForm(RestUtil.getRestTemplate(25 * 1000), ThirdPartyURL.SEARCH_IMAGE, param, String.class);
                if(response != null){
                    JSONObject jsonObject = JSONObject.parseObject(response);
                    String resultsStr = jsonObject.getString("results");
                    if(Strings.isBlank(resultsStr)){
                        Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(), "搜索结果为空",GocqActionEnum.SEND_MSG,true);
                    }else{
                        List<Results> resultList = JSONObject.parseArray(resultsStr, Results.class);
                        sort(resultList);
                        sendResult(resultList,cq,message);
                    }
                }
            }catch (Exception e){
                Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(), "搜图异常："+e.getMessage(),GocqActionEnum.SEND_MSG,true);
                log.error("搜图异常",e);
            }

        }
    }

    private void sort(List<Results> resultList){
        int size = resultList.size();
        for (int i = 0; i < size - 1; i++) {
            boolean flag = false;
            for (int f = 0; f < size - i - 1; f++) {
                if(resultList.get(f).getHeader().getSimilarity() < resultList.get(f + 1).getHeader().getSimilarity()){
                    Results results = resultList.get(f);
                    resultList.set(f,resultList.get(f + 1));
                    resultList.set(f + 1,results);
                    flag = true;
                }
            }
            if(!flag){
                break;
            }
        }
    }

    private void sendResult(List<Results> resultList,String cq,Message message){
        ArrayList<ForwardMsg> forwardMsgs = new ArrayList<>(resultList.size() + 1);
        forwardMsgs.add(CommonUtil.createForwardMsgItem(cq,message.getSelfId(), BotConfig.NAME));
        for (Results results : resultList) {
            forwardMsgs.add(CommonUtil.createForwardMsgItem(getItemMsg(results),message.getSelfId(), BotConfig.NAME));
        }

        if(MessageEventEnum.group.getType().equals(message.getMessageType())){
            // 使用合并消息
            HttpResponse response = Client.sendRestMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG, message.getGroupId(), forwardMsgs);
            if(response.getRetcode() != 0){
                forwardMsgs.remove(0);
                Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG, message.getGroupId(), forwardMsgs);
            }

        }else if(MessageEventEnum.privat.getType().equals(message.getMessageType())){
            // 私聊
            forwardMsgs.remove(0);
            Client.sendMessage(GocqActionEnum.SEND_PRIVATE_FORWARD_MSG, message.getUserId(), forwardMsgs);
        }
    }

    private String getItemMsg(Results results){
        StringBuilder strBui = new StringBuilder();
        if(results.getHeader().getSimilarity() != null){
            strBui.append(MessageFormat.format("相似度：{0}\n",results.getHeader().getSimilarity()+"%"));
        }
        if(results.getData().getTitle() != null){
            strBui.append(MessageFormat.format("标题：{0}\n",results.getData().getTitle()));
        }
        if(results.getData().getSource() != null){
            strBui.append(MessageFormat.format("来源：{0}\n",results.getData().getSource()));
        }
        if(results.getHeader().getIndex_name() != null){
            strBui.append(MessageFormat.format("数据来源：{0}\n",results.getHeader().getIndex_name()));
        }
        if(results.getData().getJp_name() != null){
            strBui.append(MessageFormat.format("日语名：{0}\n",results.getData().getJp_name()));
        }
        if(results.getData().getMaterial() != null){
            strBui.append(MessageFormat.format("出处：{0}\n",results.getData().getMaterial()));
        }
        String pixivId = results.getData().getPixiv_id();
        if(pixivId != null){
            strBui.append(MessageFormat.format("pid：{0}\n",pixivId));
        }
        if(results.getData().getMember_name() != null){
            strBui.append(MessageFormat.format("作者：{0}\n",results.getData().getMember_name()));
        }
        String creator = results.getData().getCreator();
        if(creator != null){
            List list;
            try{
                list = JSONObject.parseObject(creator, List.class);
                if(list.size() > 0){
                    strBui.append(MessageFormat.format("作者：{0}\n",list.get(0)));
                }
            }catch (Exception e){
                strBui.append(MessageFormat.format("作者：{0}\n",creator));
            }
        }
        if(results.getData().getTwitter_user_id() != null){
            strBui.append(MessageFormat.format("twitter作者id：{0}\n",results.getData().getTwitter_user_id()));
        }
        String[] ext_urls = results.getData().getExt_urls();
        if(ext_urls != null && ext_urls.length > 0){
            for (String ext_url : ext_urls) {
                strBui.append(MessageFormat.format("地址：{0}\n",ext_url));
            }
        }
        if(results.getHeader().getThumbnail() != null){
            strBui.append(MessageFormat.format("缩略图：{0}\n",results.getHeader().getThumbnail()));
        }
        if(pixivId != null){
            strBui.append(MessageFormat.format("原图链接：{0}{1}.jpg", PixivByPidHandler.u,pixivId));
        }
        return strBui.toString();
    }
}
