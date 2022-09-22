package com.haruhi.bot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haruhi.bot.entity.Checkin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CheckinMapper extends BaseMapper<Checkin> {
}
