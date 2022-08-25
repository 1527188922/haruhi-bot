package com.haruhi.bot.service.function;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haruhi.bot.entity.DisableFunction;

public interface DisableFunctionService extends IService<DisableFunction> {

    void loadBanFunction();
}
