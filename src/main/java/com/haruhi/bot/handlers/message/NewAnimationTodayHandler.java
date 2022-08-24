package com.haruhi.bot.handlers.message;

import com.alibaba.fastjson.JSONArray;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.constant.ThirdPartyURL;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.agefans.response.NewAnimationTodayResp;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.HttpClientUtil;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NewAnimationTodayHandler implements IMessageEvent {

    @Override
    public int weight() {
        return 85;
    }

    @Override
    public String funName() {
        return "今日新番";
    }

    @Override
    public boolean onMessage(final Message message,final String command) {
        if(!command.matches(RegexEnum.NEW_ANIMATION_TODAY.getValue())){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new NewAnimationTodayHandler.Task(message));
        return true;
    }

    public static class Task implements Runnable{
        private Message message;

        Task(Message message){
            this.message = message;
        }
        @Override
        public void run() {
            try {
                String responseHtml = HttpClientUtil.doGet(ThirdPartyURL.AGEFANSTV, null, 10 * 1000);
                if (Strings.isNotBlank(responseHtml)) {
                    Pattern compile = Pattern.compile("var new_anime_list = (.*?);");
                    Matcher matcher = compile.matcher(responseHtml);
                    if (matcher.find()) {
                        String group = matcher.group(1);
                        List<NewAnimationTodayResp> data = JSONArray.parseArray(group, NewAnimationTodayResp.class);
                        if (data == null || data.size() == 0) {
                            return;
                        }
                        data = data.stream().filter(e -> e.getIsnew()).collect(Collectors.toList());
                        if(data.size() > 0){
                            if(MessageEventEnum.privat.getType().equals(message.getMessage_type())){
                                sendPrivate(data,message);
                            }else if(MessageEventEnum.group.getType().equals(message.getMessage_type())){
                                sendGroup(data,message);
                            }
                        }else{
                            Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), "今日还没有新番更新", GocqActionEnum.SEND_MSG,true);
                        }
                    }
                }
            }catch (Exception e){
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), MessageFormat.format("今日新番异常",e.getMessage()), GocqActionEnum.SEND_MSG,true);
            }

        }
    }

    private static void sendGroup(List<NewAnimationTodayResp> data,Message message){
        ArrayList<ForwardMsg> param = new ArrayList<>();
        for (NewAnimationTodayResp datum : data) {
            param.add(CommonUtil.createForwardMsgItem(splicingParam(datum),message.getSelf_id(), BotConfig.NAME));
        }
        Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG,message.getGroup_id(),param);

    }
    private static void sendPrivate(List<NewAnimationTodayResp> data,Message message){
        for (NewAnimationTodayResp datum : data) {
            Client.sendMessage(message.getUser_id(),message.getGroup_id(),MessageEventEnum.privat, splicingParam(datum), GocqActionEnum.SEND_MSG,true);
        }
    }
    private static String splicingParam(NewAnimationTodayResp datum){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(datum.getName()).append("\n");
        stringBuilder.append("更新集：").append(datum.getNamefornew()).append("\n");
        stringBuilder.append(MessageFormat.format("链接：{0}/detail/{1}",ThirdPartyURL.AGEFANSTV,datum.getId()));
        return stringBuilder.toString();
    }


}
