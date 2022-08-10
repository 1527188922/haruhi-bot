package com.haruhi.bot.config.env;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
@ConditionalOnProperty(name = "env.active",havingValue = "pro")
public class ProEnvConfigImpl implements IEnvConfig{

    public ProEnvConfigImpl(){
        log.info("当前启动环境为:pro");
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
    public synchronized String resourcesImagePath() {
        if(imagePath == null){
            String path = applicationHomePath() + File.separator + "image";
            File file = new File(path);
            if(!file.exists()){
                file.mkdirs();
            }
            imagePath = path;
        }
        return imagePath;
    }
}
