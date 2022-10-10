package com.haruhi.bot.config.path;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
@ConditionalOnProperty(name = "env.active",havingValue = "pro")
public class ProPathConfigImpl implements IPathConfig {

    public ProPathConfigImpl(){
        log.info("env active : pro");
    }

    private static String homePath;
    private static String imagePath;
    private static String audioPath;

    static {
        // 加载根目录路径
        ApplicationHome ah = new ApplicationHome(ProPathConfigImpl.class);
        homePath = ah.getSource().getParentFile().toString();

        // 创建image路径
        String path = homePath + File.separator + "image";
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        imagePath = path;

        // 音频资源路径 必存在 不用创建
        audioPath = homePath + File.separator + "audio";
    }
    @Override
    public String applicationHomePath() {
        return homePath;
    }

    @Override
    public String resourcesImagePath() {
        return imagePath;
    }

    @Override
    public String resourcesAudio() {
        return audioPath;
    }
}
