package com.haruhi.bot.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.haruhi.bot.config.DataSourceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
@DS(value = DataSourceConfig.DATA_SOURCE_BOT_NAME)
public interface TableInitMapper {

    /**
     * 创建签到表
     * @return
     */
    int createCheckin(@Param("tableName") String tableName);

    /**
     * 创建禁用功能表
     * @return
     */
    int createDisableHandler(@Param("tableName") String tableName);

    /**
     * pixiv图库表
     * @param tableName
     * @return
     */
    int createPixiv(@Param("tableName") String tableName);

    /**
     * 词条表
     * @param tableName
     * @return
     */
    int createWordStrip(@Param("tableName") String tableName);

    /**
     * 话术表
     * @param tableName
     * @return
     */
    int createVerbalTricks(@Param("tableName") String tableName);

    /**
     * 群聊天记录表
     * @param tableName
     * @return
     */
    int createGroupChatHistory(@Param("tableName") String tableName);

}
