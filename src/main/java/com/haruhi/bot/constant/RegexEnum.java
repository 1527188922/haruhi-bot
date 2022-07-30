package com.haruhi.bot.constant;

public enum RegexEnum {
    CQ_CODE(".*\\[CQ:.*\\].*"),
    CHECKIN("签到|打卡"),
    SEE_FAVORABILITY("好感度|我的好感|我的好感度|查看好感|查看好感度|查看我的好感度"),
    COLLECTION("添加收藏.*|新增收藏.*|增加收藏.*"),
    COLLECTION_SPLIT("添加收藏|新增收藏|增加收藏"),
    COLLECTION_CANCEL("取消|算了|取消收藏"),
    SEARCH_IMAGE("识图|搜图");


    private String value;
    RegexEnum(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }
}
