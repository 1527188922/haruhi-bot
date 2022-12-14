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

    /**
     * 根据类全名查找bean
     * @param className
     * @param <T>
     * @return
     */
    public static <T> T getBean(String className) throws ClassNotFoundException {
        Class<T> aClass = (Class<T>) Class.forName(className);
        return applicationContextSpring.getBean(aClass);
    }

}