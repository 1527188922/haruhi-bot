package com.haruhi.bot.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haruhi.bot.config.DataSourceConfig;
import com.haruhi.bot.entity.Pixiv;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
@DS(value = DataSourceConfig.DATA_SOURCE_BOT_NAME)
public interface PixivMapper extends BaseMapper<Pixiv> {
    /**
     * 随机图片
     * @param num
     * @param isR18
     * @param tag
     * @return
     */
    List<Pixiv> roundByTagLimit(@Param("num")int num, @Param("isR18") Boolean isR18, @Param("tag") String tag);
    List<Pixiv> roundByTagAll(@Param("isR18") Boolean isR18, @Param("tag") String tag);
}
