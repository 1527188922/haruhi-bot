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
     * ??????????????????
     * ???????????????
     * @param message
     * @param param
     */
    @Override
    public void sendChatList(Message message, FindChatMessageHandler.Param param) {
        Date date = limitDate(param);
        LambdaQueryWrapper<GroupChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupChatHistory::getGroupId,message.getGroup_id()).gt(GroupChatHistory::getCreateTime,date.getTime());
        List<String> userIds = CommonUtil.getCqParams(message.getMessage(), CqCodeTypeEnum.at, "qq");
        if(!CollectionUtils.isEmpty(userIds)){
            queryWrapper.in(GroupChatHistory::getUserId,userIds);
        }
        if(FindChatMessageHandler.MessageType.IMAGE.equals(param.getMessageType())){
            // ?????????????????????
            queryWrapper.like(GroupChatHistory::getContent,"[CQ:image").like(GroupChatHistory::getContent,"subType=0");
        }else if(FindChatMessageHandler.MessageType.TXT.equals(param.getMessageType())){
            queryWrapper.notLike(GroupChatHistory::getContent,"[CQ:");
        }
        // ??????
        queryWrapper.orderByAsc(GroupChatHistory::getCreateTime);
        List<GroupChatHistory> chatList = groupChatHistoryMapper.selectList(queryWrapper);
        if(!CollectionUtils.isEmpty(chatList)){
            int limit = 80;
            if(chatList.size() > limit){
                // ??????????????????80???,????????????
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
            Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), "?????????????????????????????????",GocqActionEnum.SEND_MSG,true);
        }
    }
    private void partSend(List<GroupChatHistory> chatList, Message message){
        ArrayList<ForwardMsg> params = new ArrayList<>(chatList.size());
        for (GroupChatHistory e : chatList) {
            params.add(CommonUtil.createForwardMsgItem(e.getContent(),e.getUserId(),getName(e)));
        }
        // ??????http??????????????????
        HttpResponse response = Client.sendRestMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG, message.getGroup_id(), params);
        if(response.getRetcode() != 0){
            Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), "?????????????????????\n??????????????????\n?????????????????????????????????",GocqActionEnum.SEND_MSG,true);
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
     * ??????????????????
     * @param regexEnum
     * @param message
     */
    @Override
    public void sendWordCloudImage(WordCloudHandler.RegexEnum regexEnum,Message message) {
        // ??????????????????
        log.info("???[{}]?????????????????????...",message.getGroup_id());
        Date date = limitDate(regexEnum);
        LambdaQueryWrapper<GroupChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupChatHistory::getGroupId,message.getGroup_id()).gt(GroupChatHistory::getCreateTime,date.getTime());
        for (WordCloudHandler.RegexEnum value : WordCloudHandler.RegexEnum.values()) {
            queryWrapper.notLike(GroupChatHistory::getContent,value.getRegex());
        }
        String outPutPath = null;
        List<String> userIds = CommonUtil.getCqParams(message.getMessage(), CqCodeTypeEnum.at, "qq");
        if (!CollectionUtils.isEmpty(userIds)) {
            queryWrapper.in(GroupChatHistory::getUserId,userIds);
        }
        // ??????????????????????????????
        List<GroupChatHistory> corpus = groupChatHistoryMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(corpus)) {
            Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageEventEnum.group, MessageFormat.format("??????????????????????????????,????????????",corpus.size()),GocqActionEnum.SEND_MSG,true);
            generateComplete(message,null);
            return;
        }

        Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageEventEnum.group, MessageFormat.format("??????????????????{0}????????????????????????,????????????...",corpus.size()),GocqActionEnum.SEND_MSG,true);
        try{
            // ????????????
            long l = System.currentTimeMillis();
            List<String> collect = corpus.stream().map(GroupChatHistory::getContent).collect(Collectors.toList());
            List<String> strings = WordSlicesTask.execute(collect);
            // ?????????????????????????????????
            Map<String, Integer> map = WordCloudUtil.exclusionsWord(WordCloudUtil.setFrequency(strings));
            if(CollectionUtils.isEmpty(map)){
                Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageEventEnum.group, "?????????0???????????????????????????",GocqActionEnum.SEND_MSG,true);
                generateComplete(message,null);
                return;
            }
            long l1 = System.currentTimeMillis();
            Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageEventEnum.group, MessageFormat.format("????????????:{0}???\n??????:{1}??????\n??????????????????...",strings.size(),l1 - l),GocqActionEnum.SEND_MSG,true);
            // ??????????????????
            String fileName = regexEnum.getUnit().toString() + "-" + message.getGroup_id() + ".png";
            outPutPath = basePath + File.separator + fileName;
            WordCloudUtil.generateWordCloudImage(map,outPutPath);
            log.info("?????????????????????,??????:{}",System.currentTimeMillis() - l1);
            // ??????????????????,????????????
            KQCodeUtils instance = KQCodeUtils.getInstance();
            String imageCq = instance.toCq(CqCodeTypeEnum.image.getType(), "file=file:///" + outPutPath);
            // ???http???????????????go-cqhttp???????????????????????????????????????
            Client.sendRestMessage(message.getUser_id(), message.getGroup_id(), MessageEventEnum.group, imageCq, GocqActionEnum.SEND_MSG, false);
        }catch (Exception e){
            Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageEventEnum.group, MessageFormat.format("???????????????????????????{0}",e.getMessage()),GocqActionEnum.SEND_MSG,true);
            log.error("????????????????????????",e);
        }finally {
            generateComplete(message,outPutPath);
        }
    }
    private void generateComplete(Message message,String path){
        WordCloudHandler.lock.remove(message.getGroup_id());
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
                // ???????????????????????????
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
