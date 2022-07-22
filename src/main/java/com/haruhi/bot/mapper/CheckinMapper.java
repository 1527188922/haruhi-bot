package com.haruhi.bot.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haruhi.bot.config.DataSourceConfig;
import com.haruhi.bot.entity.Checkin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS(value = DataSourceConfig.DATA_SOURCE_BOT_NAME)
public interface CheckinMapper extends BaseMapper<Checkin> {
}
