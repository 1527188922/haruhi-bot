package com.haruhi.bot.constant;

public enum GocqActionEnum {

    SEND_MSG("send_msg","发送消息"),
    SEND_PRIVATE_MSG("send_private_msg","发送私聊消息"),
    SEND_GROUP_MSG("send_group_msg","发送群消息"),
    SEND_GROUP_FORWARD_MSG("send_group_forward_msg","转发群合并消息"),
    GET_GROUP_MEMBER_LIST("get_group_member_list","获取群成员列表");

    private String action;
    private String remarks;
    GocqActionEnum(String action, String remarks){
        this.action = action;
        this.remarks = remarks;
    }
    public String getAction(){
        return action;
    }
    public String getRemarks(){
        return remarks;
    }
}
