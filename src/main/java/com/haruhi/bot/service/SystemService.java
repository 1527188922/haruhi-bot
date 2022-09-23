package com.haruhi.bot.service;

import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.config.env.IEnvPathConfig;
import com.haruhi.bot.constant.OSEnum;
import com.haruhi.bot.dto.gocq.response.SelfInfo;
import com.haruhi.bot.utils.FileUtil;
import com.haruhi.bot.utils.GocqRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
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
            if (Strings.isNotBlank(s)) {
                File file = new File(envConfig.applicationHomePath() + File.separator + scriptName);
                FileUtil.writeText(file,s);
            }
            log.info("生成停止脚本完成:{}",scriptName);
        }
    }

    public static void loadLoginInfo(boolean reConnect){
        if (Strings.isBlank(BotConfig.SELF_ID) || reConnect) {
            loadLoginInfo();
        }
    }

    /**
     * 加载登录的qq号信息
     * 每次连接上go-cqhttp都要执行一次
     */
    public static void loadLoginInfo(){
        try {
            SelfInfo loginInfo = GocqRequestUtil.getLoginInfo();
            BotConfig.SELF_ID = loginInfo.getUser_id();
            log.info("self_id:{}",BotConfig.SELF_ID);
        }catch (Exception e){
            log.error("请求bot信息异常",e);
        }
    }
}
