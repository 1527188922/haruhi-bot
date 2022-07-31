package com.haruhi.bot.service.pixiv;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haruhi.bot.entity.Pixiv;

public interface PixivService extends IService<Pixiv> {

    /**
     * 随机一张图片
     * @param isR18 是否r18
     * @param tag 标签（模糊查询）
     * @return
     */
    Pixiv roundOneByTag(boolean isR18,String tag);
}
