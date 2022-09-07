package com.haruhi.bot.handlers.message;

import com.haruhi.bot.config.env.IEnvPathConfig;
import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.FileUtil;
import com.haruhi.bot.ws.Client;
import com.simplerobot.modules.utils.KQCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;

@Slf4j
@Component
public class ScoldMeHandler implements IMessageEvent {
    @Override
    public int weight() {
        return 88;
    }

    @Override
    public String funName() {
        return "骂我";
    }

    @Autowired
    private IEnvPathConfig envConfig;
    private static List<File> fileList;

    @PostConstruct
    private void loadAudioFileList(){
        // 初始化类时加载文件
        fileList = FileUtil.getFileList(envConfig.resourcesAudio() + File.separator + "dg");
    }


    @Override
    public boolean onMessage(final Message message,final String command) {
        String cmd;
        if(CommonUtil.isAt(message.getSelf_id(),command)){
            cmd = command.replaceAll(RegexEnum.CQ_CODE_REPLACR.getValue(),"");
        }else{
            cmd = command;
        }
        if (!cmd.trim().matches(RegexEnum.SCOLD_ME_DG.getValue())){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            int i = CommonUtil.randomInt(0, fileList.size() - 1);
            File file = fileList.get(i);
            KQCodeUtils instance = KQCodeUtils.getInstance();
            String cq = instance.toCq(CqCodeTypeEnum.record.getType(), "file=file:///" + file.toString());
            Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),cq, GocqActionEnum.SEND_MSG,false);
        });

        return true;
    }


}
