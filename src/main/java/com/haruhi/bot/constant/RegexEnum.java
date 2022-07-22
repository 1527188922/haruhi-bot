package com.haruhi.bot.constant;

public enum RegexEnum {
    CHECKIN("签到|打卡");

    private String value;
    RegexEnum(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }
}
