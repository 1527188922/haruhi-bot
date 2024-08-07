package com.haruhi.bot.service.checkin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.Params;
import com.haruhi.bot.entity.Checkin;
import com.haruhi.bot.mapper.CheckinMapper;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.DateTimeUtil;
import com.simplerobot.modules.utils.KQCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Date;

@Slf4j
@Service
public class CheckinServiceImpl extends ServiceImpl<CheckinMapper, Checkin> implements CheckinService {

    @Autowired
    private CheckinMapper checkinMapper;

    @Override
    public void checkin(Params params, Message message) {
        try {
            QueryWrapper<Checkin> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Checkin::getGroupId,message.getGroupId()).eq(Checkin::getUserId,message.getUserId());
            Checkin res = checkinMapper.selectOne(queryWrapper);
            Checkin param = new Checkin();
            if(res == null){
                // 第一次签到
                param.setGroupId(message.getGroupId());
                param.setUserId(message.getUserId());
                int favorability = CommonUtil.randomInt(3, 5);
                param.setFavorability(favorability);
                param.setDayCount(1);
                checkinMapper.insert(param);
                params.setMessage(MessageFormat.format("签到成功~，好感度+{0}\n已签到1天",favorability));
            }else{
                Date current = new Date();
                boolean today = DateTimeUtil.isSameDay(res.getLastDate(), current);
                if(!today){
                    int favorability = CommonUtil.randomInt(3, 5);
                    param.setFavorability(res.getFavorability() + favorability);
                    param.setDayCount(res.getDayCount() + 1);
                    param.setLastDate(current);
                    checkinMapper.update(param,queryWrapper);
                    params.setMessage(MessageFormat.format("签到成功~，好感度+{0}，当前好感度{1}\n已签到{2}天",favorability,param.getFavorability(),param.getDayCount()));
                }else{
                    params.setMessage(MessageFormat.format("今天已经签过到啦~当前好感度{0}\n已签到{1}天",res.getFavorability(),res.getDayCount()));
                }
            }
        }catch (Exception e){
            params.setMessage("呜呜签到失败了...");
            log.error("签到业务处理发生异常",e);
        }
    }

    @Override
    public void seeFavorability(Params params, Message message) {
        try {
            LambdaQueryWrapper<Checkin> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Checkin::getGroupId,message.getGroupId()).eq(Checkin::getUserId,message.getUserId());
            Checkin checkin = checkinMapper.selectOne(queryWrapper);
            if(checkin == null){
                params.setAutoEscape(true);
                params.setMessage("你还没有签到过呢~");
            }else{
                KQCodeUtils instance = KQCodeUtils.getInstance();
                String at = instance.toCq(CqCodeTypeEnum.at.getType(), "qq=" + message.getUserId());
                params.setMessage(MessageFormat.format("{0}当前好感度：{1}，已签到{2}天",at,checkin.getFavorability(),checkin.getDayCount()));
                params.setAutoEscape(false);
            }
        }catch (Exception e){
            params.setMessage("呜呜签到失败了...");
            params.setAutoEscape(true);
            log.error("查看好感度业务处理发生异常",e);
        }

    }
}
