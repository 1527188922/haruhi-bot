package com.haruhi.bot.constant;

public enum RegexEnum {
    CQ_CODE(".*\\[CQ:.*\\].*"),
    CQ_CODE_REPLACR("\\[CQ:.*\\]"),
    CHECKIN("签到|打卡"),
    SEE_FAVORABILITY("好感度|我的好感|我的好感度|查看好感|查看好感度|查看我的好感度"),
    COLLECTION("添加收藏.*|新增收藏.*|增加收藏.*"),
    COLLECTION_SPLIT("添加收藏|新增收藏|增加收藏"),
    COLLECTION_CANCEL("取消|算了|取消收藏"),
    SEARCH_IMAGE("识图|搜图"),
    PIXIV("pix|PIX"),
    PIXIV_R("pixr|PIXR"),
    PIXIV_PID("ppid|PPID"),
    PIXIV_UID("puid|PUID"),
    PIXIV_COUNT("pix统计|PIX统计"),
    FRIEND_SAID("朋友说|我朋友说|我朋友都说|朋友都说|朋友说过|我朋友说过|我朋友老说|我朋友总说|我朋友老是说|我朋友总是说"),
    WORD_STRIP_ADD("添加词条(.*?)答"),
    WORD_STRIP_DELETE("删除词条"),
    WORD_STRIP_SHOW("本群词条|所有词条|查看所有词条|显示所有词条");


    private String value;
    RegexEnum(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }
}
