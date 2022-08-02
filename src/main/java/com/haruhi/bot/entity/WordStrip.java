package com.haruhi.bot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haruhi.bot.config.DataSourceConfig;
import lombok.Data;

@Data
@TableName(value = DataSourceConfig.BOT_T_WORD_STRIP)
public class WordStrip {
    @TableId(value = "id",type = IdType.AUTO)
    private String id;
    private String userId;
    private String groupId;
    private String keyWord;
    private String answer;
}
