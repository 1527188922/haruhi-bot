package com.haruhi.bot.service.groupChatHistory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.HttpResponse;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.entity.GroupChatHistory;
import com.haruhi.bot.handlers.message.WordCloudHandler;
import com.haruhi.bot.handlers.message.chatHistory.FindChatMessageHandler;
import com.haruhi.bot.mapper.GroupChatHistoryMapper;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.DateTimeUtil;
import com.haruhi.bot.utils.RestUtil;
import com.haruhi.bot.ws.Client;
import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.PixelBoundaryBackground;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.image.AngleGenerator;
import com.kennycason.kumo.palette.ColorPalette;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

@Slf4j
@Service
public class GroupChatHistoryServiceImpl extends ServiceImpl<GroupChatHistoryMapper, GroupChatHistory> implements GroupChatHistoryService{

    @Autowired
    private GroupChatHistoryMapper groupChatHistoryMapper;

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
        queryWrapper.eq(GroupChatHistory::getGroupId,message.getGroup_id()).gt(GroupChatHistory::getCreateTime,date.getTime());
        List<String> userIds = CommonUtil.getCqParams(message.getMessage(), CqCodeTypeEnum.at, "qq");
        if(userIds != null && userIds.size() > 0){
            queryWrapper.in(GroupChatHistory::getUserId,userIds);
        }
        if(FindChatMessageHandler.MessageType.IMAGE.equals(param.getMessageType())){
            // 仅查询图片类型
            queryWrapper.like(GroupChatHistory::getContent,"[CQ:image").like(GroupChatHistory::getContent,"subType=0");
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

    /**
     * 发送词云图片
     * @param regexEnum
     * @param message
     */
    @Override
    public void sendWordCloudImage(WordCloudHandler.RegexEnum regexEnum,Message message) {
        Date date = limitDate(regexEnum);
        LambdaQueryWrapper<GroupChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupChatHistory::getGroupId,message.getGroup_id()).gt(GroupChatHistory::getCreateTime,date.getTime());

        List<String> userIds = CommonUtil.getCqParams(message.getMessage(), CqCodeTypeEnum.at, "qq");
        if (userIds != null && userIds.size() > 0) {
            queryWrapper.in(GroupChatHistory::getUserId,userIds);
        }
        List<GroupChatHistory> corpus = groupChatHistoryMapper.selectList(queryWrapper);
        if (corpus == null || corpus.size() == 0) {
            Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageTypeEnum.group, MessageFormat.format("该条件下没有聊天记录,无法生成",corpus.size()),GocqActionEnum.SEND_MSG,true);
            return;
        }

        Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageTypeEnum.group, MessageFormat.format("词云图片将从{0}条聊天记录中生成...",corpus.size()),GocqActionEnum.SEND_MSG,true);


        Map<String, Integer> map = handleCorpus(wordSlices(corpus));

    }
    private Map<String,Integer> handleCorpus(List<String> corpus){
        Map<String, Integer> map = new HashMap<>();
        for (String e : corpus) {

            if(e.matches(RegexEnum.CQ_CODE.getValue())){
                e = e.replaceAll(RegexEnum.CQ_CODE_REPLACR.getValue(), "");
            }
            if(map.containsKey(e)){
                Integer frequency = map.get(e) + 1;
                map.put(e,frequency);
            }else{
                map.put(e,0);
            }
        }
        return map;
    }

    private List<String> wordSlices(List<GroupChatHistory> corpus){
        StrBuilder strBuilder = new StrBuilder();
        for (GroupChatHistory e : corpus) {
            String s = e.getContent();
            if(s.matches(RegexEnum.CQ_CODE.getValue())){
                s = s.replaceAll(RegexEnum.CQ_CODE_REPLACR.getValue(), "");
            }
            strBuilder.append(s);
        }
        Map<String, Object> req = new HashMap<>();
        req.put("content",strBuilder.toString());
        String responseStr = RestUtil.sendPostRequest(RestUtil.getRestTemplate(10 * 1000), BotConfig.HTTP_URL + "/" + GocqActionEnum.GET_WORD_SLICES.getAction(), req, null, String.class);
        if(Strings.isNotBlank(responseStr)){
            JSONObject jsonObject = JSONObject.parseObject(responseStr);
            return JSONArray.parseArray(jsonObject.getJSONObject("data").getString("slices"), String.class);
        }
        return null;
    }
    private Date limitDate(WordCloudHandler.RegexEnum regexEnum){
        Date res = null;
        Date current = new Date();
        switch (regexEnum.getUnit()){
            case YEAR:
                res = DateTimeUtil.formatToDate(current,DateTimeUtil.FormatEnum.yyyy);
                break;
            case MONTH:
                res = DateTimeUtil.formatToDate(current,DateTimeUtil.FormatEnum.yyyyMM);
                break;
            case DAY:
                res = DateTimeUtil.formatToDate(current,DateTimeUtil.FormatEnum.yyyyMMdd);
                break;
            default:
                break;
        }
        return res;
    }
    private static final String templateFileName = "wordCloudTemplate1.jpg";
    /**
     * 获取词云图片
     *
     * @param corpus
     * @param pngOutputPath 图片输出路径 png结尾
     * @param shapePicPath 词云形状图片路径，其背景应为透明背景，格式为png
     * @throws IOException
     */
    private void generate(Map<String,Integer> corpus, String pngOutputPath, String shapePicPath) throws IOException {
        final List<WordFrequency> wordFrequencies = new ArrayList<WordFrequency>();
        // 加载词云有两种方式，一种是在txt文件中统计词出现的个数，另一种是直接给出每个词出现的次数，这里使用第二种
        // 文件格式如下
        for (Map.Entry<String, Integer> item : corpus.entrySet()) {
            wordFrequencies.add(new WordFrequency(item.getKey(),item.getValue()));
        }
        // 生成图片的像素大小  1 照片纵横比
        final Dimension dimension = new Dimension(1024, (int)(1024 * 1));
        final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
        // 调节词云的稀疏程度，越高越稀疏
        wordCloud.setPadding(10);

        //设置背景色
        wordCloud.setBackgroundColor(new Color(255,255,255));
        //设置背景图片
        wordCloud.setBackground(new PixelBoundaryBackground(shapePicPath));

        // 颜色模板，不同频率的颜色会不同
        wordCloud.setColorPalette(new ColorPalette(new Color( 234,106,40), new Color(255,146,70), new Color(255,182,69), new Color(255,232,113), new Color(255,251,192), new Color(254,255,233)));
        // 设置字体
        java.awt.Font font = new java.awt.Font("楷体", 0, 20);
        wordCloud.setKumoFont(new KumoFont(font));
        // 设置偏转角，角度为0时，字体都是水平的
        // wordCloud.setAngleGenerator(new AngleGenerator(0, 90, 9));
        wordCloud.setAngleGenerator(new AngleGenerator(0));
        // 字体的大小范围，最小是多少，最大是多少
        wordCloud.setFontScalar(new SqrtFontScalar(5, 40));
        wordCloud.build(wordFrequencies);
        wordCloud.writeToFile(pngOutputPath);
    }

}
