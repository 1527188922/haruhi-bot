package com.haruhi.bot.handlers.message;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.dto.gocq.response.ForwardMsg;
import com.haruhi.bot.dto.searchImage.response.Results;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.event.message.IOnMessageEvent;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.RestUtil;
import com.haruhi.bot.ws.Client;
import com.simplerobot.modules.utils.KQCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class SearchImageHandler implements IOnMessageEvent {

    @Override
    public int weight() {
        return 98;
    }

    private static Long timeout = 10L* 1000L;

    private Map<String,Long> timeoutMap = new ConcurrentHashMap<>(10);
    private Map<String,String> cqtMap = new ConcurrentHashMap<>(10);
    @Override
    public boolean matches(final Message message,final String command,final AtomicInteger total) {
        synchronized (total){
            if(total.get() == 0){

                KQCodeUtils utils = KQCodeUtils.getInstance();
                String cq = utils.getCq(command, CqCodeTypeEnum.image.getType(), 0);
                String key = CommonUtil.getKey(message.getUser_id(), message.getGroup_id());

                if(timeoutMap.get(key) != null ){
                    if(System.currentTimeMillis() - timeoutMap.get(key) < timeout){
                        if(cq != null){
                            cqtMap.put(key,cq);
                            return true;
                        }
                    }else{
                        timeoutMap.remove(key);
                    }
                }
                boolean matches = false;
                String[] split = RegexEnum.SEARCH_IMAGE.getValue().split("\\|");
                for (String s : split) {
                    if(command.startsWith(s)){
                        matches = true;
                        break;
                    }
                }
                if(!matches){
                    return false;
                }

                if(cq != null){
                    cqtMap.put(key,cq);
                    return true;
                }else{
                    timeoutMap.put(key,System.currentTimeMillis());
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),"图呢！", GocqActionEnum.SEND_MSG,true);
                    total.incrementAndGet();
                    return false;
                }
            }
            return false;
        }
    }

    @Override
    public void onMessage(Message message, String command) {
        String key = CommonUtil.getKey(message.getUser_id(), message.getGroup_id());
        if(key != null){
            Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),"开始搜图...",GocqActionEnum.SEND_MSG,true);
            ThreadPoolFactory.getCommandHandlerThreadPool().execute(new SearchImageHandler.searchImageTask(message,cqtMap.get(key)));
            after(key);
        }
    }

    private void after(String key){
        timeoutMap.remove(key);
        cqtMap.remove(key);
    }

    public static class searchImageTask implements Runnable{
        private static String url = "https://saucenao.com/search.php";
        private Message message;
        private String cq;

        searchImageTask(Message message,String cq){
            this.message = message;
            this.cq = cq;
        }
        @Override
        public void run() {
            KQCodeUtils instance = KQCodeUtils.getInstance();
            String imageUrl = instance.getParam(this.cq, "url",CqCodeTypeEnum.image.getType(),0);

            LinkedMultiValueMap<String,Object> param = new LinkedMultiValueMap<>();
            param.add("output_type",2);
            param.add("api_key",BotConfig.SEARCH_IMAGE_KEY);
            param.add("testmode",1);
            param.add("numres",6);
            param.add("db",99);
            param.add("url",imageUrl);
            try {
                String response = RestUtil.sendPostForm(RestUtil.getRestTemplate(30 * 1000), url, param, String.class);
                if(response != null){
                    JSONObject jsonObject = JSONObject.parseObject(response);
                    String resultsStr = jsonObject.getString("results");
                    if(CommonUtil.isBlank(resultsStr)){
                        Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), "搜索结果为空",GocqActionEnum.SEND_MSG,true);
                    }else{
                        List<Results> resultList = JSONObject.parseArray(resultsStr, Results.class);
                        sort(resultList);
                        sendResult(resultList);
                    }
                }
            }catch (NullPointerException e){
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), "搜图异常：null",GocqActionEnum.SEND_MSG,true);
            }catch (Exception e){
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), "搜图异常："+e.getMessage(),GocqActionEnum.SEND_MSG,true);
                log.error("搜图异常",e);
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
        private void sendResult(List<Results> resultList){
            ArrayList<ForwardMsg> forwardMsgs = new ArrayList<>();
            ForwardMsg forwardMsg = new ForwardMsg();
            ForwardMsg.Data data = new ForwardMsg.Data();
            data.setContent(cq);
            data.setName(BotConfig.NAME);
            data.setUin(message.getSelf_id());
            forwardMsg.setData(data);
            forwardMsgs.add(forwardMsg);
            for (Results results : resultList) {
                ForwardMsg forwardMsgInner = new ForwardMsg();
                ForwardMsg.Data dataInner = new ForwardMsg.Data();
                dataInner.setContent(getItemMsg(results));
                dataInner.setName(BotConfig.NAME);
                dataInner.setUin(message.getSelf_id());
                forwardMsgInner.setData(dataInner);
                forwardMsgs.add(forwardMsgInner);
            }

            if(MessageTypeEnum.group.getType().equals(message.getMessage_type())){
                // 使用合并消息
                Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG, message.getGroup_id(), forwardMsgs);

            }else if(MessageTypeEnum.privat.getType().equals(message.getMessage_type())){
                // 私聊
                for (int i = 1; i < forwardMsgs.size(); i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Client.sendMessage(message.getUser_id(), message.getGroup_id(), MessageTypeEnum.privat, forwardMsgs.get(i).getData().getContent(),GocqActionEnum.SEND_MSG,true);
                }
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
            if(results.getData().getPixiv_id() != null){
                strBui.append(MessageFormat.format("pid：{0}\n",results.getData().getPixiv_id()));
            }
            if(results.getData().getMember_name() != null){
                strBui.append(MessageFormat.format("作者：{0}\n",results.getData().getMember_name()));
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
                strBui.append(MessageFormat.format("略缩图：{0}\n",results.getHeader().getThumbnail()));
            }
            return strBui.toString();
        }
    }
}
