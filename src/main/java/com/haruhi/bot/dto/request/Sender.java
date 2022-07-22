package com.haruhi.bot.dto.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class Sender implements Serializable {
    private int age;
    private String area;
    private String card;
    private String level;
    private String role;
    private String nickname;
    private String sex;
    private String title;
    private String user_id;

}
