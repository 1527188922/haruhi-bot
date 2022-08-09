package com.haruhi.bot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haruhi.bot.config.DataSourceConfig;
import lombok.Data;

@Data
@TableName(value = DataSourceConfig.BOT_T_GROUP_CHAT_HISTORY)
public class GroupChatHistory {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private String card;
    private String nickname;
    private String messageId;
    private String groupId;
    private String userId;
    private String content;
    private Long createTime;
}
