package com.haruhi.bot.dto.agefans.response;

import lombok.Data;

@Data
public class NewAnimationTodayResp {
    private String id;
    private Boolean isnew;
    private Integer wd;
    private String name;
    private String mtime;
    private String namefornew;

}
