package com.haruhi.bot.service.groupChatHistory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.dto.gocq.response.ForwardMsg;
import com.haruhi.bot.entity.GroupChatHistory;
import com.haruhi.bot.handlers.message.chatHistory.FindChatMessageHandler;
import com.haruhi.bot.mapper.GroupChatHistoryMapper;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.DateTimeUtil;
import com.haruhi.bot.ws.Client;
import com.simplerobot.modules.utils.KQCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class GroupChatHistoryServiceImpl extends ServiceImpl<GroupChatHistoryMapper, GroupChatHistory> implements GroupChatHistoryService{

    @Autowired
    private GroupChatHistoryMapper groupChatHistoryMapper;

    @Override
    public void sendChatList(Message message, FindChatMessageHandler.Param param, String[] atCqs) {
        Date date = limitDate(param);
        LambdaQueryWrapper<GroupChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupChatHistory::getGroupId,message.getGroup_id()).and(e->e.gt(GroupChatHistory::getCreateTime,date.getTime()));
        if(atCqs != null && atCqs.length > 0){
            // 根据群qq用户查找
            KQCodeUtils instance = KQCodeUtils.getInstance();
            List<String> userIds = new ArrayList<>();
            for (String cq : atCqs) {
                String qq = instance.getParam(cq, "qq");
                userIds.add(qq);
            }
            queryWrapper.and(e->e.in(GroupChatHistory::getUserId,userIds));
        }
        if(FindChatMessageHandler.MessageType.IMAGE.equals(param.getMessageType())){
            // 仅查询图片类型
            queryWrapper.and(e->e.like(GroupChatHistory::getContent,"[CQ:image"));
        }
        // 升序
        queryWrapper.orderByAsc(GroupChatHistory::getCreateTime);
        List<GroupChatHistory> chatList = groupChatHistoryMapper.selectList(queryWrapper);
        if(chatList != null && chatList.size() > 0){
            ArrayList<ForwardMsg> params = new ArrayList<>();
            for (GroupChatHistory e : chatList) {
                params.add(CommonUtil.createForwardMsgItem(e.getContent(),e.getUserId(),getName(e)));
            }
            Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG,message.getGroup_id(),params);
        }else{
            Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), "该条件下没有聊天记录。",GocqActionEnum.SEND_MSG,true);
        }
    }
    private String getName(GroupChatHistory e){
        try {
            if(Strings.isNotBlank(e.getCard().trim())){
                return e.getCard();
            }
            if(Strings.isNotBlank(e.getNickname().trim())){
                return e.getNickname();
            }
        }catch (Exception ex){
        }
        return "noname";
    }
    private Date limitDate(FindChatMessageHandler.Param param){
        Date res = null;
        Date current = new Date();
        switch (param.getUnit()){
            case DAY:
                if (param.getNum() > 15) {
                    param.setNum(15);
                }
                res = DateTimeUtil.addDay(current,-(param.getNum()));
                break;
            case HOUR:
                int limit = 15 * 24;
                if (param.getNum() > limit) {
                    param.setNum(limit);
                }
                res = DateTimeUtil.addHour(current,-(param.getNum()));
                break;
            default:
                break;
        }
        return res;
    }

}
