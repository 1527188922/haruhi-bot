package com.haruhi.bot.factory;

import com.haruhi.bot.service.checkin.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceFactory {

    public static CheckinService checkinService;

    @Autowired
    public void setCheckinService(CheckinService checkinService){
        ServiceFactory.checkinService = checkinService;
    }
}
