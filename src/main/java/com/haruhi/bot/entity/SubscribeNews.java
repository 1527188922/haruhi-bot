package com.haruhi.bot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haruhi.bot.config.DataSourceConfig;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = DataSourceConfig.BOT_T_SUBSCRIBE_NEWS)
public class SubscribeNews {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private String subscriber;
    private String groupId;
    // 1:群 2:私
    private Integer type;
    private Date createTime;
}
