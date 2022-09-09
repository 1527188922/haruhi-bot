package com.haruhi.bot.dto.gocq.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class SelfInfo implements Serializable {
    private String user_id;
    private String nickname;
}
