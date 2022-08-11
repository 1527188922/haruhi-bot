package com.haruhi.bot.handlers.notice;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.notice.IPokeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 处理戳一戳
 */
@Slf4j
@Component
public class PokeHandler implements IPokeEvent {

    @Override
    public boolean onPoke(final Message message) {
        log.info("收到戳一戳:{}", JSONObject.toJSONString(message));
        return true;
    }
}
