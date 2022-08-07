package com.haruhi.bot.service.verbalTricks;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haruhi.bot.entity.VerbalTricks;

public interface VerbalTricksService extends IService<VerbalTricks> {

    void loadVerbalTricks();
}
