package com.haruhi.bot.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
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
    int createDisableFunction(@Param("tableName") String tableName);

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

    /**
     * 戳一戳回复表
     * @param tableName
     * @return
     */
    int createPokeReply(@Param("tableName") String tableName);

    /**
     * 订阅新闻表
     * @param tableName
     * @return
     */
    int createSubscribeNews(@Param("tableName") String tableName);
}
