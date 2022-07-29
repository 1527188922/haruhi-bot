package com.haruhi.bot.dto.aiChat.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatResp implements Serializable {

    private int result;
    private String content;
}
