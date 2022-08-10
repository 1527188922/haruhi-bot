package com.haruhi.bot.config.env;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
@ConditionalOnProperty(name = "env.active",havingValue = "dev")
public class DevEnvConfigImpl implements IEnvConfig{
    public DevEnvConfigImpl(){
        log.info("当前启动环境为:dev");
    }
    private static String homePath;
    private static String imagePath;
    @Override
    public synchronized String applicationHomePath() {
        if(homePath == null){
            ApplicationHome ah = new ApplicationHome(getClass());
            homePath = ah.getSource().getParentFile().toString();
        }
        return homePath;
    }

    @Override
    public String resourcesImagePath() {
        if(imagePath == null){
            File directory = new File("src/main/resources");
            try {
                imagePath = directory.getCanonicalPath() + File.separator + "image";
            } catch (IOException e) {
                log.error("获取resources绝对路径异常,环境:dev",e);
            }
        }
        return imagePath;
    }
}
