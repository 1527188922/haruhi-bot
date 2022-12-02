package com.haruhi.bot.dto.gocq.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 群成员信息
 */
@Data
public class GroupMember implements Serializable {
    private int age;
    private String area;
    private String card;
    @JSONField(name = "card_changeable")
    private boolean cardChangeable;
    @JSONField(name = "group_id")
    private String groupId;
    @JSONField(name = "join_time")
    private long joinTime;
    @JSONField(name = "last_sent_time")
    private long lastSentTime;
    private String level;
    private String nickname;
    private String role;
    private String sex;
    @JSONField(name = "shut_up_timestamp")
    private int shutUpTimestamp;
    private String title;
    @JSONField(name = "title_expire_time")
    private int titleExpireTime;
    private boolean unfriendly;
    @JSONField(name = "user_id")
    private String userId;
}
