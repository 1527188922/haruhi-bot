package com.haruhi.bot.service.function;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haruhi.bot.entity.DisableFunction;

public interface DisableFunctionService extends IService<DisableFunction> {

    /**
     * 从数据库加载全局禁用的功能
     */
    void loadGlobalBanFunction();

    /**
     * 从数据库加载群禁用的功能
     */
    void loadGroupBanFunction();
}
