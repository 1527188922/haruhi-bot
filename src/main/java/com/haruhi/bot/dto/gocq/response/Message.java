package com.haruhi.bot.dto.gocq.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class Message implements Serializable {

    private String post_type;
    private String meta_event_type;
    private String message_type;
    private String notice_type;
    // 操作人id 比如群管理员a踢了一个人,那么该值为a的qq号
    private String operator_id;
    private Long time;
    private String self_id;
    private String sub_type;
    private String user_id;
    private String sender_id;
    private String group_id;
    private String target_id;
    private String message;
    private String raw_message;
    private Integer font;
    private Sender sender;
    private String message_id;
    private Integer message_seq;
    private String anonymous;
}
