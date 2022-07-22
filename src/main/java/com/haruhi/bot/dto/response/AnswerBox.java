package com.haruhi.bot.dto.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class AnswerBox<T> implements Serializable {
    private String action;
    private T params;
    private String echo;
}
