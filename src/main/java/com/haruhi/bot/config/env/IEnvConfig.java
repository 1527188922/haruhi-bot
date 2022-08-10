package com.haruhi.bot.config.env;

public interface IEnvConfig {
    /**
     * 应用jar所在路径
     * 区分dev和pro环境
     * @return
     */
    String applicationHomePath();

    String resourcesImagePath();
}
