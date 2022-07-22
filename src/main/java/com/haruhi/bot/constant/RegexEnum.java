package com.haruhi.bot.constant;

public enum RegexEnum {
    SIGNIN("签到|打卡","签到");

    private String value;
    private String remake;
    RegexEnum(String value,String remake){
        this.value = value;
        this.remake = remake;
    }
    public String getValue(){
        return value;
    }
    public String getRemake(){
        return remake;
    }
}
