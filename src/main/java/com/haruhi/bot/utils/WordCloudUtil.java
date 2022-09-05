package com.haruhi.bot.utils;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.constant.ThirdPartyURL;
import com.haruhi.bot.dto.xml.bilibili.PlayerInfoResp;
import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.image.AngleGenerator;
import com.kennycason.kumo.palette.ColorPalette;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.logging.log4j.util.Strings;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WordCloudUtil {
    private WordCloudUtil(){}

    private static String noSupport = "你的QQ暂不支持查看&#91;转发多条消息&#93;，请期待后续版本。";
    /**
     * 请求gocq接口进行分词
     * @param originCorpus 还未分词的词料
     * @return
     */
    public static List<String> wordSlices(List<String> originCorpus){
        StrBuilder strBuilder = new StrBuilder();
        for (String e : originCorpus) {
            strBuilder.append(replace(e));
        }
        return request(strBuilder.toString());
    }
    public static List<String> wordSlices(String s){
        return request(replace(s));
    }
    private static String replace(String s){
        return s.trim().replaceAll(RegexEnum.CQ_CODE_REPLACR.getValue(), "").replace(noSupport,"").replaceAll("&#93;|&#91;","").replaceAll("\\s*|\r|\n|\t","");
    }
    private static List<String> request(String s){
        if(Strings.isBlank(s) || s.length() <= 1){
            return null;
        }
        return GocqRequestUtil.getWordSlices(s);
    }

    /**
     * 设置词的权重
     * @param corpus 已经分词的词料
     * @return key:词语 value:权重
     */
    public static Map<String,Integer> setFrequency(List<String> corpus){
        Map<String, Integer> map = new HashMap<>();
        for (String e : corpus) {
            if(e.length() <= 1){
                continue;
            }
            if(map.containsKey(e)){
                Integer frequency = map.get(e) + 1;
                map.put(e,frequency);
            }else{
                map.put(e,1);
            }
        }
        return map;
    }

    /**
     * 生成词云图片
     * @param corpus
     * @param pngOutputPath 图片输出路径 png结尾
     */
    public static void generateWordCloudImage(Map<String,Integer> corpus, String pngOutputPath) {
        final List<WordFrequency> wordFrequencies = new ArrayList<>(corpus.size());
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
//        wordCloud.setBackground(new PixelBoundaryBackground(shapePicPath));

        // 颜色模板，不同频率的颜色会不同
        wordCloud.setColorPalette(new ColorPalette(new Color(255, 68, 51), new Color(208, 79, 8), new Color(225, 98, 50), new Color(231, 126, 88), new Color(175, 129, 3), new Color(243, 150, 9)));
        // 设置字体
        java.awt.Font font = new java.awt.Font("楷体", 0, 20);
        wordCloud.setKumoFont(new KumoFont(font));
        // 设置偏转角，角度为0时，字体都是水平的
        // wordCloud.setAngleGenerator(new AngleGenerator(0, 90, 9));
        wordCloud.setAngleGenerator(new AngleGenerator(0));
        // 字体的大小范围，最小是多少，最大是多少
        wordCloud.setFontScalar(new SqrtFontScalar(5, 80));
        wordCloud.build(wordFrequencies);
        wordCloud.writeToFile(pngOutputPath);
    }

    /**
     * 通过视频bv获取cid
     * @param bv
     * @return
     */
    public static PlayerInfoResp getPlayerInfo(String bv){
        Map<String, Object> param = new HashMap<>(2);
        param.put("bvid",bv);
        param.put("jsonp","jsonp");
        String responseStr = RestUtil.sendGetRequest(RestUtil.getRestTemplate(), ThirdPartyURL.PLAYER_CID, param, String.class);
        if(Strings.isNotBlank(responseStr)){
            JSONObject jsonObject = JSONObject.parseObject(responseStr);
            String data = jsonObject.getJSONArray("data").getString(0);
            return JSONObject.parseObject(data,PlayerInfoResp.class);
        }
        return null;
    }

    /**
     * 根据视频cid获取弹幕
     * @param cid
     * @return
     */
    public static List<String> getChatList(String cid){
        Map<String, Object> param = new HashMap<>();
        param.put("oid",cid);
        String responseSre = HttpClientUtil.doGet(ThirdPartyURL.BULLET_CHAR, param,10 * 1000);
        if(Strings.isNotBlank(responseSre)){
            Pattern compile = Pattern.compile("\">(.*?)</d>");
            Matcher matcher = compile.matcher(responseSre);
            List<String> res = new ArrayList<>();
            while (matcher.find()) {
                res.add(matcher.group(1));
            }
            return res;
        }
        return null;
    }

    /**
     * 根据av号获取bv号
     * @param av
     * @return
     */
    public static String getBvByAv(String av){

        String responseSre = HttpClientUtil.doGet(ThirdPartyURL.BILIBILI_URL+"/" + av,null,10 * 1000);
        if (Strings.isNotBlank(responseSre)) {
            Pattern compile = Pattern.compile("<meta data-vue-meta=\"true\" itemprop=\"url\" content=\"https://www.bilibili.com/video/(.*?)/\">");
            Matcher matcher = compile.matcher(responseSre);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

}
