package com.haruhi.bot.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haruhi.bot.config.DataSourceConfig;
import com.haruhi.bot.entity.Pixiv;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
@DS(value = DataSourceConfig.DATA_SOURCE_BOT_NAME)
public interface PixivMapper extends BaseMapper<Pixiv> {
    Pixiv roundOneByTag(@Param("isR18") Boolean isR18,@Param("tag") String tag);
}
