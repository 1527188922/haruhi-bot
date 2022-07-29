package com.haruhi.bot.service.checkin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.dto.gocq.response.Answer;
import com.haruhi.bot.entity.Checkin;
import com.haruhi.bot.mapper.CheckinMapper;
import com.haruhi.bot.utils.DateTimeUtil;
import com.simplerobot.modules.utils.KQCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Random;

@Slf4j
@Service
public class CheckinServiceImpl extends ServiceImpl<CheckinMapper, Checkin> implements CheckinService {

    @Autowired
    private CheckinMapper checkinMapper;

    @Override
    public void checkin(Answer answer, Message message) {
        try {
            QueryWrapper<Checkin> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Checkin::getGroupId,message.getGroup_id()).eq(Checkin::getUserQq,message.getUser_id());
            Checkin res = checkinMapper.selectOne(queryWrapper);
            Checkin param = new Checkin();
            if(res == null){
                // 第一次签到
                param.setGroupId(message.getGroup_id());
                param.setUserQq(message.getUser_id());
                int favorability = randomFavorability(3, 5);
                param.setFavorability(favorability);
                param.setDayCount(1);
                checkinMapper.insert(param);
                answer.setMessage(MessageFormat.format("签到成功~，好感度+{0}\n已签到1天",favorability));
            }else{
                Date current = new Date();
                boolean today = DateTimeUtil.isSameDay(res.getLastDate(), current);
                if(!today){
                    int favorability = randomFavorability(3, 5);
                    param.setFavorability(res.getFavorability() + favorability);
                    param.setDayCount(res.getDayCount() + 1);
                    param.setLastDate(current);
                    checkinMapper.update(param,queryWrapper);
                    answer.setMessage(MessageFormat.format("签到成功~，好感度+{0}，当前好感度{1}\n已签到{2}天",
                            favorability,param.getFavorability(),param.getDayCount()));
                }else{
                    answer.setMessage(MessageFormat.format("今天已经签过到啦~当前好感度{0}\n已签到{1}天",
                            res.getFavorability(),res.getDayCount()));
                }
            }
        }catch (Exception e){
            answer.setMessage("呜呜签到失败了...");
            log.error("签到业务处理发生异常",e);
        }
    }

    @Override
    public void seeFavorability(Answer answer,Message message) {
        try {
            LambdaQueryWrapper<Checkin> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Checkin::getGroupId,message.getGroup_id()).eq(Checkin::getUserQq,message.getUser_id());
            Checkin checkin = checkinMapper.selectOne(queryWrapper);
            if(checkin == null){
                answer.setAuto_escape(true);
                answer.setMessage("你还没有签到过呢~");
            }else{
                KQCodeUtils instance = KQCodeUtils.getInstance();
                String at = instance.toCq(CqCodeTypeEnum.at.getType(), "qq=" + message.getUser_id());
                answer.setMessage(MessageFormat.format("{0}当前好感度：{1}，已签到{2}天",at,checkin.getFavorability(),checkin.getDayCount()));
                answer.setAuto_escape(false);
            }
        }catch (Exception e){
            answer.setMessage("呜呜签到失败了...");
            answer.setAuto_escape(true);
            log.error("查看好感度业务处理发生异常",e);
        }

    }

    int randomFavorability(int start,int end){
        Random random = new Random();
        return random.nextInt(end - start + 1) + start;
    }
}
