package com.haruhi.bot.dto.gocq.response;

import lombok.Data;

import java.util.Collection;

@Data
public class Answer {
    private String message_type;
    private String user_id;
    private String group_id;
    private Object message;
    private Collection messages;
    private boolean auto_escape;
}
