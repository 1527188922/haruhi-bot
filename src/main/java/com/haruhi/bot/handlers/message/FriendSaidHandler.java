package com.haruhi.bot.handlers.message;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.dto.gocq.response.ForwardMsg;
import com.haruhi.bot.dto.gocq.response.GroupMember;
import com.haruhi.bot.event.message.IOnGroupMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.RestUtil;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FriendSaidHandler implements IOnGroupMessageEvent {
    @Override
    public int weight() {
        return 99;
    }
    private String say;
    @Override
    public synchronized boolean matches(final Message message,final String command,final AtomicInteger total) {
        String[] split = RegexEnum.FRIEND_SAID.getValue().split("\\|");
        for (String s : split) {
            if (command.startsWith(s)) {
                say = command.replace(s,"").trim();
                if("".equals(say)){
                    return false;
                }
                say = say.replaceFirst("他|她|它","我");
                return true;
            }
        }

        return false;
    }

    @Override
    public void onGroup(Message message, String command) {
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new FriendSaidHandler.SayTask(message,this.say));
        this.say = null;
    }

    public static class SayTask implements Runnable{
        private Message message;
        private String say;
        SayTask(Message message,String say){
            this.say = new String(say);
            this.message = message;
        }
        @Override
        public void run() {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("group_id",message.getGroup_id());
                String responseStr = RestUtil.sendPostRequest(RestUtil.getRestTemplate(5 * 1000), BotConfig.HTTP_URL + "/" + GocqActionEnum.GET_GROUP_MEMBER_LIST.getAction(), params, null, String.class);
                if (responseStr == null) {
                    String errorMsg = "获取群成员列表失败:null";
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageTypeEnum.group,errorMsg,GocqActionEnum.SEND_MSG,true);
                    log.error(errorMsg);
                    return;
                }
                JSONObject responseJsonObj = JSONObject.parseObject(responseStr);
                List<GroupMember> data = JSONArray.parseArray(responseJsonObj.getString("data"), GroupMember.class).stream().filter(e -> !e.getUser_id().equals(message.getUser_id()) && !e.getUser_id().equals(message.getSelf_id())).collect(Collectors.toList());
                int i = CommonUtil.randomInt(0, data.size() - 1);
                GroupMember friend = data.get(i);
                send(friend);
            }catch (Exception e){
                Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageTypeEnum.group,"这个朋友不听话...",GocqActionEnum.SEND_MSG,true);
                log.error("朋友说发生异常",e);
            }

        }

        /**
         * 发送合并消息
         * @param friend
         */
        private void send(GroupMember friend){
            List<ForwardMsg> params = new ArrayList<>();
            ForwardMsg item = new ForwardMsg();
            ForwardMsg.Data data = new ForwardMsg.Data();
            data.setUin(friend.getUser_id());
            data.setName("朋友");
            data.setContent(this.say);
            item.setData(data);
            params.add(item);
            Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG,friend.getGroup_id(),params);
            Client.sendMessage(null,message.getGroup_id(),"",null,GocqActionEnum.GET_GROUP_MEMBER_LIST,true);
        }
    }
}
