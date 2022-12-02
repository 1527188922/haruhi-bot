package com.haruhi.bot.service;

import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.config.path.AbstractPathConfig;
import com.haruhi.bot.dto.gocq.response.SelfInfo;
import com.haruhi.bot.service.function.DisableFunctionService;
import com.haruhi.bot.service.pokeReply.PokeReplyService;
import com.haruhi.bot.service.verbalTricks.VerbalTricksService;
import com.haruhi.bot.service.wordStrip.WordStripService;
import com.haruhi.bot.utils.FileUtil;
import com.haruhi.bot.utils.GocqRequestUtil;
import com.haruhi.bot.utils.system.SystemInfo;
import com.haruhi.bot.utils.system.SystemUtil;
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
    private AbstractPathConfig envConfig;

    @Autowired
    private WordStripService wordStripService;
    @Autowired
    private VerbalTricksService verbalTricksService;
    @Autowired
    private PokeReplyService pokeReplyService;
    @Autowired
    private DisableFunctionService disableFunctionService;

    public void writeStopScript(){
        if(SystemUtil.PROFILE_RPO.equals(SystemInfo.PROFILE)){
            String s = null;
            String scriptName = null;
            if(SystemUtil.IS_OS_LINUX || SystemUtil.IS_OS_MAC){
                s = MessageFormat.format("kill -9 {0}",SystemInfo.PID);
                scriptName = "stop.sh";
            }else if (SystemUtil.IS_OS_WINDOWS){
                s = MessageFormat.format("taskkill /pid {0} -t -f",SystemInfo.PID);
                scriptName = "stop.bat";
            }else {
                log.warn("当前系统不支持生成停止脚本:{}",SystemInfo.OS_NAME);
                return;
            }
            if (Strings.isNotBlank(s)) {
                File file = new File(envConfig.applicationHomePath() + File.separator + scriptName);
                FileUtil.writeText(file,s);
            }
            log.info("生成停止脚本完成:{}",scriptName);
        }
    }

    public synchronized static void loadLoginInfo(boolean reConnect){
        if (Strings.isBlank(BotConfig.SELF_ID) || reConnect) {
            loadLoginInfo();
        }
    }

    /**
     * 加载登录的qq号信息
     * 每次连接上go-cqhttp都要执行一次
     */
    public synchronized static void loadLoginInfo(){
        try {
            SelfInfo loginInfo = GocqRequestUtil.getLoginInfo();
            BotConfig.SELF_ID = loginInfo.getUserId();
            log.info("self_id:{}",BotConfig.SELF_ID);
        }catch (Exception e){
            log.error("请求bot信息异常",e);
        }
    }

    public synchronized void loadCache(){
       try {
           // 加载词条到内存
           wordStripService.loadWordStrip();
           // 加载话术到内存
           verbalTricksService.loadVerbalTricks();
           // 加载戳一戳回复
           pokeReplyService.loadPokeReply();
           // 加载全局被禁用的功能
           disableFunctionService.loadGlobalBanFunction();
           // 加载群禁用功能
           disableFunctionService.loadGroupBanFunction();
           log.info("加载缓存完成");
       }catch (Exception e){
           log.error("加载缓存异常",e);
       }
    }
}
