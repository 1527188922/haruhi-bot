package com.haruhi.bot.service.news;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haruhi.bot.dto.news.response.NewsBy163Resp;
import com.haruhi.bot.entity.SubscribeNews;

import java.util.List;

public interface SubscribeNewsService extends IService<SubscribeNews> {

    List<NewsBy163Resp> requestNewsBy163();

    void sendGroup(List<NewsBy163Resp> list,List<String> groupIds);

    void sendPrivate(List<NewsBy163Resp> list,List<String> userIds);
}
