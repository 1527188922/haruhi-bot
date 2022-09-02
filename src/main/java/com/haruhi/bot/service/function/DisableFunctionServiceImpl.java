package com.haruhi.bot.service.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.dispenser.MessageDispenser;
import com.haruhi.bot.entity.DisableFunction;
import com.haruhi.bot.event.message.IMessageEventType;
import com.haruhi.bot.mapper.DisableFunctionMapper;
import com.haruhi.bot.utils.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DisableFunctionServiceImpl extends ServiceImpl<DisableFunctionMapper, DisableFunction> implements DisableFunctionService{

    @Autowired
    private DisableFunctionMapper disableFunctionMapper;

    @Override
    public void loadGlobalBanFunction() {
        LambdaQueryWrapper<DisableFunction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DisableFunction::getGlobal,true);
        List<DisableFunction> banFuns = disableFunctionMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(banFuns)) {
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

    @Override
    public void loadGroupBanFunction() {
        LambdaQueryWrapper<DisableFunction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DisableFunction::getGlobal,false).ne(DisableFunction::getGroupId,"").isNotNull(DisableFunction::getGroupId);
        List<DisableFunction> banFuns = disableFunctionMapper.selectList(queryWrapper);
        if(!CollectionUtils.isEmpty(banFuns)){
            // 根据群号分组转map
            Map<String, List<DisableFunction>> map = banFuns.stream().collect(Collectors.groupingBy(DisableFunction::getGroupId, Collectors.toList()));
            Map<String, List<String>> res = new ConcurrentHashMap<>(map.size());
            for (Map.Entry<String, List<DisableFunction>> listEntry : map.entrySet()) {
                List<DisableFunction> value = listEntry.getValue();
                if(!CollectionUtils.isEmpty(value)){
                    List<String> classNames = new ArrayList<>(value.size());
                    // 遍历map的value,将DisableFunction对象的className都取出来,添加进新的list中去
                    for (DisableFunction item : value) {
                        classNames.add(item.getClassName());
                    }
                    res.put(listEntry.getKey(),classNames);
                }
            }
            MessageDispenser.setGroupBanFunction(res);
        }
    }
}
