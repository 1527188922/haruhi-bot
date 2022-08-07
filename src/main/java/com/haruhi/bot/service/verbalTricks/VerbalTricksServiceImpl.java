package com.haruhi.bot.service.verbalTricks;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.entity.VerbalTricks;
import com.haruhi.bot.handlers.message.VerbalTricksHandler;
import com.haruhi.bot.mapper.VerbalTricksMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VerbalTricksServiceImpl extends ServiceImpl<VerbalTricksMapper, VerbalTricks> implements VerbalTricksService {

    @Autowired
    private VerbalTricksMapper verbalTricksMapper;

    @Override
    public void loadVerbalTricks() {
        List<VerbalTricks> all = verbalTricksMapper.selectList(null);
        if(all.size() > 0){
            Map<String, List<VerbalTricks>> groupMap = all.stream().collect(Collectors.groupingBy(VerbalTricks::getRegex, Collectors.toList()));
            VerbalTricksHandler.setCache(groupMap);
        }

    }
}
