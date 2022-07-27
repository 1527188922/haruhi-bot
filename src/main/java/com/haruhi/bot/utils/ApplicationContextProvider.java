package com.haruhi.bot.utils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContextSpring;
    @Override
    public synchronized void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContextSpring = applicationContext;
    }
    public static <T> T getBean(Class<T> clazz) {

        return applicationContextSpring.getBean(clazz);
    }

    public static Object getBean(String className) {
        try {
            Class<?> aClass = Class.forName(className);
            return applicationContextSpring.getBean(aClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}