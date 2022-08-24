package com.haruhi.bot.handlers.message;

import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.constant.TimeUnitEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IGroupMessageEvent;

import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.groupChatHistory.GroupChatHistoryService;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class WordCloudHandler implements IGroupMessageEvent {

    @Override
    public int weight() {
        return 90;
    }

    @Override
    public String funName() {
        return "群词云";
    }

    public static volatile Map<String, Integer> lock = new ConcurrentHashMap<>(4);

    @Autowired
    private GroupChatHistoryService groupChatHistoryService;

    private RegexEnum matching(final String command){
        String s = command.replaceAll(com.haruhi.bot.constant.RegexEnum.CQ_CODE_REPLACR.getValue(), "").trim();
        if(s.matches(RegexEnum.YEAR.regex)){
            return RegexEnum.YEAR;
        }else if (s.matches(RegexEnum.MONTH.regex)){
            return RegexEnum.MONTH;
        }else if(s.matches(RegexEnum.WEEK.regex)){
            return RegexEnum.WEEK;
        }else if(s.matches(RegexEnum.DAY.regex)){
            return RegexEnum.DAY;
        }
        return null;
    }
    @Override
    public boolean onGroup(final Message message,final String command) {
        RegexEnum matching = matching(command);
        if (matching == null) {
            return false;
        }
        if(lock.containsKey(message.getGroup_id())){
            Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageEventEnum.group,"词云正在生成中...莫着急", GocqActionEnum.SEND_MSG,true);
            return true;
        }else{
            lock.put(message.getGroup_id(),1);
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new WordCloudHandler.Task(groupChatHistoryService,matching,message));
        return true;
    }
    public static class Task implements Runnable{
        private GroupChatHistoryService groupChatHistoryService;
        private RegexEnum regexEnum;
        private Message message;
        public Task( GroupChatHistoryService groupChatHistoryService,RegexEnum regexEnum,Message message){
            this.groupChatHistoryService = groupChatHistoryService;
            this.regexEnum = regexEnum;
            this.message = message;
        }
        @Override
        public void run() {
            groupChatHistoryService.sendWordCloudImage(regexEnum, message);
        }
    }
    public enum RegexEnum{
        YEAR("年度词云",TimeUnitEnum.YEAR),
        MONTH("本月词云",TimeUnitEnum.MONTH),
        WEEK("本周词云",TimeUnitEnum.WEEK),
        DAY("今日词云",TimeUnitEnum.DAY);

        private String regex;
        private TimeUnitEnum unit;
        RegexEnum(String regex, TimeUnitEnum unit){
            this.regex = regex;
            this.unit = unit;
        }
        public TimeUnitEnum getUnit(){
            return unit;
        }
        public String getRegex(){
            return regex;
        }
    }

}
