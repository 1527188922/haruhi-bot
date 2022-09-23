package com.haruhi.bot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haruhi.bot.entity.GroupChatHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupChatHistoryMapper extends BaseMapper<GroupChatHistory> {
}
