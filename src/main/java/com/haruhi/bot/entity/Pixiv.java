package com.haruhi.bot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haruhi.bot.config.DataSourceConfig;
import lombok.Data;

@Data
@TableName(value = DataSourceConfig.BOT_T_PIXIV)
public class Pixiv {
    @TableId(value = "id",type = IdType.AUTO)
    private String id;
    private Integer pid;
    private String title;
    private Integer width;
    private Integer height;
    @TableField("`view`")
    private Integer view;
    private Integer bookmarks;
    private String imgUrl;
    // 图片md5 haruhi库来的
    private String imgP;
    private Integer uid;
    private String author;
    private Boolean isR18;
    private String tags;
}
