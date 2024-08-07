package com.haruhi.bot.handlers.message;

import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.dto.gocq.response.GroupMember;
import com.haruhi.bot.event.message.IGroupMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.GocqRequestUtil;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FriendSaidHandler implements IGroupMessageEvent {
    @Override
    public int weight() {
        return 84;
    }

    @Override
    public String funName() {
        return "朋友说";
    }

    private String say;

    public boolean matching(final String command) {
        String[] split = RegexEnum.FRIEND_SAID.getValue().split("\\|");
        for (String s : split) {
            if (command.startsWith(s)) {
                say = command.replaceFirst(s,"").trim();
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
    public boolean onGroup(final Message message,final String command) {
        if(!matching(command)){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new SayTask(message,this.say));
        this.say = null;
        return true;
    }

    private class SayTask implements Runnable{
        private Message message;
        private String say;
        SayTask(Message message,String say){
            this.say = say;
            this.message = message;
        }
        @Override
        public void run() {
            try {
                List<GroupMember> groupMemberList = GocqRequestUtil.getGroupMemberList(message.getGroupId(), message.getUserId(), message.getSelfId());
                if(CollectionUtils.isEmpty(groupMemberList)){
                    Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group,"你哪来的朋友？",GocqActionEnum.SEND_MSG,true);
                    return;
                }
                int i = CommonUtil.randomInt(0, groupMemberList.size() - 1);
                GroupMember friend = groupMemberList.get(i);
                send(friend,say);
            }catch (Exception e){
                Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group,"这个朋友不听话...",GocqActionEnum.SEND_MSG,true);
                log.error("朋友说发生异常",e);
            }

        }

    }
    /**
     * 发送合并消息
     * @param friend
     */
    private void send(GroupMember friend,String say){
        List<ForwardMsg> params = new ArrayList<>(1);
        params.add(CommonUtil.createForwardMsgItem(say,friend.getUserId(),"朋友"));
        Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG,friend.getGroupId(),params);
    }
}
