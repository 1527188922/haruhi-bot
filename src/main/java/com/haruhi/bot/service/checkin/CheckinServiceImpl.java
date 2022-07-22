package com.haruhi.bot.service.checkin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.dto.response.Answer;
import com.haruhi.bot.entity.Checkin;
import com.haruhi.bot.mapper.CheckinMapper;
import com.haruhi.bot.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CheckinServiceImpl extends ServiceImpl<CheckinMapper, Checkin> implements CheckinService {

    @Autowired
    private CheckinMapper checkinMapper;

    @Override
    public void checkin(Answer answer) {
        try {
            QueryWrapper<Checkin> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Checkin::getGroupId,answer.getGroup_id()).eq(Checkin::getUserQq,answer.getUser_id());
            Checkin res = checkinMapper.selectOne(queryWrapper);
            Checkin checkin = new Checkin();
            checkin.setGroupId(answer.getGroup_id());
            checkin.setUserQq(answer.getUser_id());
            int favorability = randomFavorability(3, 5);
            checkin.setFavorability(favorability);
            if(res == null){
                // 第一次签到
                checkin.setDayCount(1);
                checkinMapper.insert(checkin);
                answer.setMessage("签到成功，好感度+"+favorability+"~");
            }else{
                Date today = new Date();
                boolean sameDay = DateTimeUtil.isSameDay(checkin.getLastDate(), today);
                if(!sameDay){
                    checkin.setDayCount(checkin.getDayCount() + 1);
                    checkin.setLastDate(today);
                    checkinMapper.update(checkin,queryWrapper);
                    answer.setMessage("签到成功，好感度+"+favorability+"~");
                }else{
                    answer.setMessage("今天已经签过到啦~");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            answer.setMessage("呜呜签到失败了...");
        }
    }

    int randomFavorability(int start,int end){

        return (int)(end + Math.random() * (end - start));
    }
}
