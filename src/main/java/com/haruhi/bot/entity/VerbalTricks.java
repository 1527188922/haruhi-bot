package com.haruhi.bot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haruhi.bot.config.DataSourceConfig;
import lombok.Data;

@Data
@TableName(value = DataSourceConfig.BOT_T_VERBAL_TRICKS)
public class VerbalTricks {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private String regex;
    private String answer;
}
