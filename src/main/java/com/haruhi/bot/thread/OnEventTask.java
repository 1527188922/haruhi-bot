package com.haruhi.bot.thread;

import com.haruhi.bot.constant.PostTypeEnum;
import com.haruhi.bot.dispenser.MessageDispenser;
import com.haruhi.bot.dispenser.NoticeDispenser;
import com.haruhi.bot.dto.gocq.response.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OnEventTask implements Runnable{

    private Message messageBean;
    public OnEventTask(Message messageBean){
        this.messageBean = messageBean;
    }

    @Override
    public void run() {
        try {
            if(PostTypeEnum.message.toString().equals(messageBean.getPost_type())){
                // 普通消息
                final String command = messageBean.getMessage();
                log.info("[{}]收到来自用户[{}]的消息:{}",messageBean.getMessage_type(),messageBean.getUser_id(),command);
                if(command != null){
                    MessageDispenser.onEvent(messageBean,command);
                }
            }else if(PostTypeEnum.notice.toString().equals(messageBean.getPost_type())){
                // bot通知
                NoticeDispenser.onEvent(messageBean);
            } else if(PostTypeEnum.meta_event.toString().equals(messageBean.getPost_type())){
                // 系统消息
            }else{

            }
        }catch (Exception e){
            log.error("解析消息时异常",e);
        }
    }
}
