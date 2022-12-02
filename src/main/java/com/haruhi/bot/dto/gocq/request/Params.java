package com.haruhi.bot.dto.gocq.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Collection;

@Data
public class Params {
    @JSONField(name = "message_type")
    private String messageType;
    @JSONField(name = "user_id")
    private String userId;
    @JSONField(name = "group_id")
    private String groupId;
    private Object message;
    private Collection messages;
    @JSONField(name = "auto_escape")
    private boolean autoEscape;
}
