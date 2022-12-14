package com.haruhi.bot.service.groupChatHistory;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.entity.GroupChatHistory;
import com.haruhi.bot.handlers.message.WordCloudHandler;
import com.haruhi.bot.handlers.message.chatHistory.FindChatMessageHandler;

public interface GroupChatHistoryService extends IService<GroupChatHistory> {

    /**
     * 根据时间发送聊天记录
     * @param message
     * @param param
     */
    void sendChatList(Message message, FindChatMessageHandler.Param param);

    void sendWordCloudImage(WordCloudHandler.RegexEnum regexEnum,Message message);
}
