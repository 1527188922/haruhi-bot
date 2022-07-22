package com.haruhi.bot.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.haruhi.bot.config.DataSourceConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS(value = DataSourceConfig.DATA_SOURCE_BOT_NAME)
public interface TableInitMapper {

    /**
     * 创建签到表
     * @return
     */
    int createCheckin();

}
