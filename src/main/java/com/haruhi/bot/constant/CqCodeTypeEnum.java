package com.haruhi.bot.constant;

public enum CqCodeTypeEnum {
    at("at"),
    image("image"),
    face("face"),
    forward("forward"),
    reply("reply");


    private String type;
    CqCodeTypeEnum(String type){
        this.type = type;
    }
    public String getType(){
        return type;
    }
}
