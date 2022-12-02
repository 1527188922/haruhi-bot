package com.haruhi.bot.dto.gocq.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

@Data
public class SelfInfo implements Serializable {
    @JSONField(name = "user_id")
    private String userId;
    private String nickname;
}
