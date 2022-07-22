package com.haruhi.bot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haruhi.bot.config.DataSourceConfig;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName(value = DataSourceConfig.BOT_T_CHECKIN)
public class Checkin {

    @TableId(value = "id",type = IdType.AUTO)
    private int id;
    @TableField(value = "`user_qq`")
    private String userQq;
    private String groupId;
    private int dayCount;
    private int favorability;
    private Date firstDate;
    private Date lastDate;

}
