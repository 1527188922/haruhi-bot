package com.haruhi.bot.service.function;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.dispenser.MessageDispenser;
import com.haruhi.bot.entity.DisableFunction;
import com.haruhi.bot.event.message.IMessageEventType;
import com.haruhi.bot.mapper.DisableFunctionMapper;
import com.haruhi.bot.utils.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DisableFunctionServiceImpl extends ServiceImpl<DisableFunctionMapper, DisableFunction> implements DisableFunctionService{

    @Autowired
    private DisableFunctionMapper disableFunctionMapper;

    @Override
    public void loadBanFunction() {
        List<DisableFunction> banFuns = disableFunctionMapper.selectList(null);
        if (banFuns != null && banFuns.size() > 0) {
            try {
                for (DisableFunction banFun : banFuns) {
                    IMessageEventType bean = ApplicationContextProvider.getBean(banFun.getClassName());
                    MessageDispenser.detach(bean.getClass());
                }
            }catch (ClassNotFoundException e){
                log.error("加载禁用功能异常",e);
            }
        }
    }
}
