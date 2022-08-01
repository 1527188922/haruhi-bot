package com.haruhi.bot.constant;

public enum RegexEnum {
    CQ_CODE(".*\\[CQ:.*\\].*"),
    CHECKIN("签到|打卡"),
    SEE_FAVORABILITY("好感度|我的好感|我的好感度|查看好感|查看好感度|查看我的好感度"),
    COLLECTION("添加收藏.*|新增收藏.*|增加收藏.*"),
    COLLECTION_SPLIT("添加收藏|新增收藏|增加收藏"),
    COLLECTION_CANCEL("取消|算了|取消收藏"),
    SEARCH_IMAGE("识图|搜图"),
    PIXIV("pix|PIX"),
    FRIEND_SAID("朋友说|我朋友说|我朋友都说|朋友都说|朋友说过|我朋友说过|我朋友老说|我朋友总说|我朋友老是说|我朋友总是说");


    private String value;
    RegexEnum(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }
}
