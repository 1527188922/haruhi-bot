package com.haruhi.bot.service;

import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.config.env.IEnvPathConfig;
import com.haruhi.bot.constant.OSEnum;
import com.haruhi.bot.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.MessageFormat;


@Slf4j
@Service
public class SystemService {

    @Autowired
    private IEnvPathConfig envConfig;

    public void writeStopScript(){
        if(BotConfig.PRO.get()){
            String s = null;
            String scriptName = null;
            if(OSEnum.linux.equals(BotConfig.osName)){
                s = MessageFormat.format("kill -9 {0}",BotConfig.PID);
                scriptName = "stop.sh";
            }else if (OSEnum.windows.equals(BotConfig.osName)){
                s = MessageFormat.format("taskkill /pid {0} -t -f",BotConfig.PID);
                scriptName = "stop.bat";
            }
            if (StringUtils.isNotBlank(s)) {
                File file = new File(envConfig.applicationHomePath() + File.separator + scriptName);
                FileUtil.writeText(file,s);
            }
            log.info("生成停止脚本完成:{}",scriptName);
        }
    }
}
