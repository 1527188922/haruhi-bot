package com.haruhi.bot.handlers.message;

import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.constant.ThirdPartyURL;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.HttpClientUtil;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class BtSearchHandler implements IMessageEvent {
    @Override
    public int weight() {
        return 87;
    }

    @Override
    public String funName() {
        return "bt搜索";
    }

    @Override
    public boolean onMessage(final Message message,final String command) {
        Pattern compile = Pattern.compile(RegexEnum.BT_SEARCH_HAS_PAGE.getValue());
        Matcher matcher = compile.matcher(command);
        Integer page = null;
        String keyword = null;
        if (matcher.find()) {
            try {
                page = Integer.valueOf(matcher.group(1));
                String str1 = command.substring(0, command.indexOf("页"));
                keyword = command.substring(str1.length() + 1, command.length());
            }catch (Exception e){
                // bt{page}页 page不是整数
            }
        }else if(command.startsWith(RegexEnum.BT_SEARCH.getValue())){
            page = 1;
            keyword = command.replaceFirst(RegexEnum.BT_SEARCH.getValue(),"");
        }
        if(Strings.isBlank(keyword)){
            return false;
        }
        Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),"开始搜索...", GocqActionEnum.SEND_MSG,true);
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new Task(message,keyword,page));
        return true;
    }

    private class Task implements Runnable{
        private Message message;
        private Integer page;
        private String keyword;
        Task(Message message,String keyword,Integer page){
            this.message = message;
            this.page = page;
            this.keyword = keyword;
        }
        @Override
        public void run() {
            try {
                String htmlStr = HttpClientUtil.doGet(HttpClientUtil.getHttpClient(10 * 1000),MessageFormat.format(ThirdPartyURL.BT_SEARCH + "/s/{0}_rel_{1}.html", keyword, page), null);
                if(Strings.isBlank(htmlStr)){
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),"bt搜索请求发生异常", GocqActionEnum.SEND_MSG,true);
                    return;
                }
                Document document = Jsoup.parse(htmlStr);
                Elements list = document.getElementsByClass("search-item");
                if (CollectionUtils.isEmpty(list)) {
                    noData(message,keyword);
                    return;
                }
                List<String> res = new ArrayList<>(list.size());
                for (Element element : list) {
                    Elements a = element.getElementsByTag("a");
                    if (CollectionUtils.isEmpty(a)) {
                        continue;
                    }
                    StringBuilder strBuilder = new StringBuilder();
                    Element title = a.get(0);
                    String detailHref = title.attr("href");
                    // 追加标题
                    strBuilder.append(title.text()).append("\n");
                    String s = ThirdPartyURL.BT_SEARCH + detailHref;
                    try {
                        // 请求详情链接
                        requestDetail(strBuilder,s);
                    }catch (Exception e){
                        log.error("bt获取详情异常:{}",s,e);
                        continue;
                    }
                    res.add(strBuilder.toString());
                }
                if(res.size() == 0){
                    noData(message,keyword);
                    return;
                }
                List<ForwardMsg> param = new ArrayList<>(res.size());
                for (String re : res) {
                    param.add(CommonUtil.createForwardMsgItem(re,message.getSelf_id(),BotConfig.NAME));
                }
                if(MessageEventEnum.group.getType().equals(message.getMessage_type())){
                    Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG,message.getGroup_id(),param);
                }else if(MessageEventEnum.privat.getType().equals(message.getMessage_type())){
                    Client.sendMessage(GocqActionEnum.SEND_PRIVATE_FORWARD_MSG,message.getUser_id(),param);
                }
            }catch (Exception e){
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),MessageFormat.format("bt搜索异常:{0}",e.getMessage()), GocqActionEnum.SEND_MSG,true);
                log.error("bt搜图异常",e);
            }

        }
    }
    private void noData(Message message,String keyword){
        Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),"没搜到：" + keyword, GocqActionEnum.SEND_MSG,true);
    }

    /**
     * 获取资源详情
     * @param strBuilder
     * @param detailHref
     * @return
     */
    private void requestDetail(StringBuilder strBuilder,String detailHref) throws Exception{

        String html = HttpClientUtil.doGet(HttpClientUtil.getHttpClient(5 * 1000),detailHref, null);
        Document document = Jsoup.parse(html);
        Element fileDetail = document.getElementsByClass("fileDetail").get(0);
        Element size = fileDetail.getElementsByTag("p").get(1);
        Element time = fileDetail.getElementsByTag("p").get(2);
        Element magnetLink = fileDetail.getElementById("down-url");
        strBuilder.append(size.text()).append("\n");
        strBuilder.append(time.text()).append("\n");
        strBuilder.append(magnetLink.text()).append("：\n");
        strBuilder.append(magnetLink.attr("href"));
    }
}
