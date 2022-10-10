package com.haruhi.bot.config.path;

public interface IPathConfig {
    /**
     * 应用jar所在路径
     * 区分dev和pro环境
     * @return
     */
    String applicationHomePath();

    /**
     * 图片路径
     * @return
     */
    String resourcesImagePath();

    // 音频路径
    String resourcesAudio();
}