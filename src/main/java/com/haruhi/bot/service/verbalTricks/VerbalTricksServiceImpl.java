package com.haruhi.bot.service.verbalTricks;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.entity.VerbalTricks;
import com.haruhi.bot.handlers.message.VerbalTricksHandler;
import com.haruhi.bot.mapper.VerbalTricksMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VerbalTricksServiceImpl extends ServiceImpl<VerbalTricksMapper, VerbalTricks> implements VerbalTricksService {

    @Autowired
    private VerbalTricksMapper verbalTricksMapper;

    @Override
    public void loadVerbalTricks() {
        List<VerbalTricks> all = verbalTricksMapper.selectList(null);
        if(!CollectionUtils.isEmpty(all)){
            Map<String, List<VerbalTricks>> groupMap = all.stream().collect(Collectors.groupingBy(VerbalTricks::getRegex, Collectors.toList()));
            VerbalTricksHandler.setCache(groupMap);
            log.info("加载全局回复数据到内存成功，数据总量：{}，分组后的数量：{}",all.size(),groupMap.size());
        }

    }
}
