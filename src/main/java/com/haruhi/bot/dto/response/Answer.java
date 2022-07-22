package com.haruhi.bot.dto.response;

import lombok.Data;

@Data
public class Answer {
    private String message_type;
    private String user_id;
    private String group_id;
    private Object message;
    private boolean auto_escape;
}
