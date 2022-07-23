package com.haruhi.bot.constant;

public enum RegexEnum {
    CHECKIN("签到|打卡"),
    SEE_FAVORABILITY("好感度|我的好感|我的好感度|查看好感|查看好感度|查看我的好感度");

    private String value;
    RegexEnum(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }
}
