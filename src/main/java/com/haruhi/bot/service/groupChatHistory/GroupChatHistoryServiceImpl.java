package com.haruhi.bot.service.groupChatHistory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.config.path.AbstractPathConfig;
import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.response.HttpResponse;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.entity.GroupChatHistory;
import com.haruhi.bot.handlers.message.WordCloudHandler;
import com.haruhi.bot.handlers.message.chatHistory.FindChatMessageHandler;
import com.haruhi.bot.mapper.GroupChatHistoryMapper;
import com.haruhi.bot.thread.WordSlicesTask;

import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.DateTimeUtil;
import com.haruhi.bot.utils.FileUtil;
import com.haruhi.bot.utils.WordCloudUtil;
import com.haruhi.bot.ws.Client;
import com.simplerobot.modules.utils.KQCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupChatHistoryServiceImpl extends ServiceImpl<GroupChatHistoryMapper, GroupChatHistory> implements GroupChatHistoryService{

    @Autowired
    private GroupChatHistoryMapper groupChatHistoryMapper;

    @Autowired
    private AbstractPathConfig envConfig;

    private static String basePath;

    private static Executor pool =  new ThreadPoolExecutor(2,2,60L, TimeUnit.SECONDS,new ArrayBlockingQueue(8),new CustomizableThreadFactory("pool-chat-history-"),new ThreadPoolExecutor.CallerRunsPolicy());

    @PostConstruct
    private void mkdirs(){
        basePath = envConfig.resourcesImagePath() + File.separator + "wordCloud";
        File file = new File(basePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 发送聊天历史
     * 群合并类型
     * @param message
     * @param param
     */
    @Override
    public void sendChatList(Message message, FindChatMessageHandler.Param param) {
        Date date = limitDate(param);
        LambdaQueryWrapper<GroupChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupChatHistory::getGroupId,message.getGroupId()).gt(GroupChatHistory::getCreateTime,date.getTime());
        List<String> userIds = CommonUtil.getCqParams(message.getMessage(), CqCodeTypeEnum.at, "qq");
        if(!CollectionUtils.isEmpty(userIds)){
            queryWrapper.in(GroupChatHistory::getUserId,userIds);
        }
        if(FindChatMessageHandler.MessageType.IMAGE.equals(param.getMessageType())){
            // 仅查询图片类型
            queryWrapper.like(GroupChatHistory::getContent,"[CQ:image").like(GroupChatHistory::getContent,"subType=0");
        }else if(FindChatMessageHandler.MessageType.TXT.equals(param.getMessageType())){
            queryWrapper.notLike(GroupChatHistory::getContent,"[CQ:");
        }
        // 升序
        queryWrapper.orderByAsc(GroupChatHistory::getCreateTime);
        List<GroupChatHistory> chatList = groupChatHistoryMapper.selectList(queryWrapper);
        if(!CollectionUtils.isEmpty(chatList)){
            int limit = 80;
            if(chatList.size() > limit){
                // 记录条数多于80张,分开发送
                List<List<GroupChatHistory>> lists = CommonUtil.averageAssignList(chatList, limit);
                for (List<GroupChatHistory> list : lists) {
                    pool.execute(()->{
                        partSend(list,message);
                    });
                }
            }else{
                partSend(chatList,message);
            }
        }else{
            Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(), "该条件下没有聊天记录。",GocqActionEnum.SEND_MSG,true);
        }
    }
    private void partSend(List<GroupChatHistory> chatList, Message message){
        ArrayList<ForwardMsg> params = new ArrayList<>(chatList.size());
        for (GroupChatHistory e : chatList) {
            params.add(CommonUtil.createForwardMsgItem(e.getContent(),e.getUserId(),getName(e)));
        }
        // 使用http接口发送消息
        HttpResponse response = Client.sendRestMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG, message.getGroupId(), params);
        if(response.getRetcode() != 0){
            Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(), "消息发送失败：\n可能被风控；\n消息可能包含敏感内容；",GocqActionEnum.SEND_MSG,true);
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

    /**
     * 发送词云图片
     * @param regexEnum
     * @param message
     */
    @Override
    public void sendWordCloudImage(WordCloudHandler.RegexEnum regexEnum,Message message) {
        // 解析查询条件
        log.info("群[{}]开始生成词云图...",message.getGroupId());
        Date date = limitDate(regexEnum);
        LambdaQueryWrapper<GroupChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupChatHistory::getGroupId,message.getGroupId()).gt(GroupChatHistory::getCreateTime,date.getTime());
        for (WordCloudHandler.RegexEnum value : WordCloudHandler.RegexEnum.values()) {
            queryWrapper.notLike(GroupChatHistory::getContent,value.getRegex());
        }
        String outPutPath = null;
        List<String> userIds = CommonUtil.getCqParams(message.getMessage(), CqCodeTypeEnum.at, "qq");
        if (!CollectionUtils.isEmpty(userIds)) {
            queryWrapper.in(GroupChatHistory::getUserId,userIds);
        }
        // 从数据库查询聊天记录
        List<GroupChatHistory> corpus = groupChatHistoryMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(corpus)) {
            Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group, MessageFormat.format("该条件下没有聊天记录,无法生成",corpus.size()),GocqActionEnum.SEND_MSG,true);
            generateComplete(message,null);
            return;
        }

        Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group, MessageFormat.format("词云图片将从{0}条聊天记录中生成,开始分词...",corpus.size()),GocqActionEnum.SEND_MSG,true);
        try{
            // 开始分词
            long l = System.currentTimeMillis();
            List<String> collect = corpus.stream().map(GroupChatHistory::getContent).collect(Collectors.toList());
            List<String> strings = WordSlicesTask.execute(collect);
            // 设置权重和排除指定词语
            Map<String, Integer> map = WordCloudUtil.exclusionsWord(WordCloudUtil.setFrequency(strings));
            if(CollectionUtils.isEmpty(map)){
                Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group, "分词为0，本次不生成词云图",GocqActionEnum.SEND_MSG,true);
                generateComplete(message,null);
                return;
            }
            long l1 = System.currentTimeMillis();
            Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group, MessageFormat.format("分词完成:{0}条\n耗时:{1}毫秒\n开始生成图片...",strings.size(),l1 - l),GocqActionEnum.SEND_MSG,true);
            // 开始生成图片
            String fileName = regexEnum.getUnit().toString() + "-" + message.getGroupId() + ".png";
            outPutPath = basePath + File.separator + fileName;
            WordCloudUtil.generateWordCloudImage(map,outPutPath);
            log.info("生成词云图完成,耗时:{}",System.currentTimeMillis() - l1);
            // 生成图片完成,发送图片
            KQCodeUtils instance = KQCodeUtils.getInstance();
            String imageCq = instance.toCq(CqCodeTypeEnum.image.getType(), "file=file:///" + outPutPath);
            // 走http发送，这样go-cqhttp发送完成之后，连接才会结束
            Client.sendRestMessage(message.getUserId(), message.getGroupId(), MessageEventEnum.group, imageCq, GocqActionEnum.SEND_MSG, false);
        }catch (Exception e){
            Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group, MessageFormat.format("生成词云图片异常：{0}",e.getMessage()),GocqActionEnum.SEND_MSG,true);
            log.error("生成词云图片异常",e);
        }finally {
            generateComplete(message,outPutPath);
        }
    }
    private void generateComplete(Message message,String path){
        WordCloudHandler.lock.remove(message.getGroupId());
        FileUtil.deleteFile(path);
    }

    private Date limitDate(WordCloudHandler.RegexEnum regexEnum){
        Date res = null;
        Date current = new Date();
        switch (regexEnum.getUnit()){
            case YEAR:
                res = DateTimeUtil.formatToDate(current, DateTimeUtil.PatternEnum.yyyy);
                break;
            case MONTH:
                res = DateTimeUtil.formatToDate(current, DateTimeUtil.PatternEnum.yyyyMM);
                break;
            case WEEK:
                // 获取本周第一天日期
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(current);
                calendar.set(Calendar.DAY_OF_WEEK, 2);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                res = calendar.getTime();
                break;
            case DAY:
                res = DateTimeUtil.formatToDate(current, DateTimeUtil.PatternEnum.yyyyMMdd);
                break;
            default:
                break;
        }
        return res;
    }

}
