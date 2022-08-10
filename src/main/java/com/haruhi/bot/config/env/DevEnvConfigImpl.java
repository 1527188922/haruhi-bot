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

    static {
        setHomePath();
        setImagePath();
    }
    @Override
    public synchronized String applicationHomePath() {

        return homePath;
    }
    private static void setHomePath(){
        ApplicationHome ah = new ApplicationHome(DevEnvConfigImpl.class);
        homePath = ah.getSource().getParentFile().toString();
    }
    private static void setImagePath(){
        File directory = new File("src/main/resources");
        try {
            imagePath = directory.getCanonicalPath() + File.separator + "build\\image";
            File file = new File(imagePath);
            if(!file.exists()){
                file.mkdirs();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String resourcesImagePath() {

        return imagePath;
    }
}
